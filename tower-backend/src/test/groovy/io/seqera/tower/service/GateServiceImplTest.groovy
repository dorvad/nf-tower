/*
 * Copyright (c) 2019, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.service

import javax.inject.Inject

import io.micronaut.context.ApplicationContext
import io.micronaut.test.annotation.MicronautTest
import io.seqera.mail.Attachment
import io.seqera.tower.domain.User
import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest
class GateServiceImplTest extends Specification {

    @Inject
    ApplicationContext context

    def 'should bind properties' () {
        given:
        GateServiceImpl userService = context.getBean(GateServiceImpl)
        expect:
        userService.appName == 'Nextflow Tower'
        userService.serverUrl == 'http://localhost:8000'
    }

    def 'should load text template' () {
        given:
        def binding = [
                user: 'Mr Bean',
                app_name: 'Nextflow Tower',
                auth_url: 'https://tower.com/login?d78a8ds',
                server_url:'http://host.com']
        def service = Spy(GateServiceImpl)
        when:
        def text = service.getTextTemplate(binding)
        then:
        text.contains('Hi Mr Bean,')
        text.contains('https://tower.com/login?d78a8ds')
        text.contains('http://host.com')
        text.contains('This email was sent by Nextflow Tower')
    }

    def 'should load html template' () {
        given:
        def binding = [
                user: 'Mr Bean',
                app_name: 'Nextflow Tower',
                auth_url: 'https://tower.com/login?1234',
                server_url:'https://tower.nf']
        def service = Spy(GateServiceImpl)
        when:
        def text = service.getHtmlTemplate(binding)
        then:
        text.contains('Hi Mr Bean,')
        text.contains('href="https://tower.com/login?1234"')
        text.contains('https://tower.nf')
        text.contains('This email was sent by Nextflow Tower')
    }

    def 'should load logo attachment' () {
        given:
        def service = new GateServiceImpl()
        when:
        def attach = service.getLogoAttachment()
        then:
        attach.resource == '/io/seqera/tower/service/tower-logo.png'
        attach.params.contentId == '<tower-logo>'
        attach.params.disposition == 'inline'
        then:
        this.class.getResource(attach.resource) != null
    }


    def 'should build auth email' () {
        given:
        def ATTACH = new Attachment(new File('LOGO'))
        def RECIPIENT = 'alice@domain.com'
        def user = new User(email: RECIPIENT, userName:'Mr Foo', authToken: 'xyz')
        def mailer = Mock(MailService)
        def service = new GateServiceImpl()
        service.mailService = mailer
        service.appName = 'Nextflow Tower'
        service.serverUrl = 'http://localhost:1234'

        when:
        def mail = service.buildAccessEmail(user)

        then:
        mail.subject == 'Nextflow Tower Sign in'
        mail.to == RECIPIENT
        mail.attachments == [ATTACH]
        // text email
        mail.text.startsWith('Hi Mr Foo,')
        mail.text.contains('http://localhost:1234/auth?email=alice%40domain.com&authToken=xyz')
        mail.text.contains('This email was sent by Nextflow Tower\nhttp://localhost')
        // html email
        mail.body.contains('Hi Mr Foo,')
        mail.body.contains('http://localhost:1234/auth?email=alice%40domain.com&authToken=xyz')
    }

    def 'should build a new user email' () {
        given:
        def ATTACH = new Attachment(new File('LOGO'))
        def email = 'alice@domain.com'
        def user = new User(email: email, userName:'xyz', id: 123)
        def mailer = Mock(MailService)
        def service = new GateServiceImpl()
        service.mailService = mailer
        service.appName = 'Nextflow Tower'
        service.serverUrl = 'http://localhost:1234'
        service.contactEmail = 'admin@host.com'

        when:
        def mail = service.buildNewUserEmail(user)

        then:
        mail.subject == 'New user registration'
        mail.to == 'admin@host.com'
        mail.attachments == [ATTACH]
        // text email
        mail.text.startsWith('Hi,')
        mail.text.contains("- user id: 123")
        mail.text.contains("- user name: xyz")
        mail.text.contains("- user email: alice@domain.com")
        // html email
        mail.body.contains('Hi,')
        mail.body.contains("<li>user id: 123")
        mail.body.contains("<li>user name: xyz")
        mail.body.contains("<li>user email: alice@domain.com")
    }


    def 'should encode url' () {
        given:
        def service = new GateServiceImpl ()
        service.serverUrl = 'http://host.com'

        expect:
        service.buildAccessUrl(new User(email:EMAIL, authToken: 'abc')) == EXPECTED

        where:
        EMAIL               | EXPECTED
        'yo@gmail.com'      | 'http://host.com/auth?email=yo%40gmail.com&authToken=abc'
        'yo+xyz@gmail.com'  | 'http://host.com/auth?email=yo%2Bxyz%40gmail.com&authToken=abc'
    }

}
