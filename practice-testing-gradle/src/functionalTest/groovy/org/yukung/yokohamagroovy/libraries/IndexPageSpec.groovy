package org.yukung.yokohamagroovy.libraries
import geb.spock.GebReportingSpec
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(loader = SpringApplicationContextLoader, classes = LibrariesApplication)
@WebIntegrationTest("server.port:8080")
class IndexPageSpec extends GebReportingSpec {

    def "Hello, Library!が表示されていること"() {
        when:
        to IndexPage

        then:
        $('p').text() == "Hello, Library!"
    }
}
