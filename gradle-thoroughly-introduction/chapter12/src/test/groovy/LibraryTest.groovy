/*
 * This Spock specification was auto generated by running 'gradle init --type groovy-library'
 * by 'yukung' at '15/11/08 14:10' with Gradle 2.8
 *
 * @author yukung, @date 15/11/08 14:10
 */

import spock.lang.Specification

class LibraryTest extends Specification{
    def "someLibraryMethod returns true"() {
        setup:
        Library lib = new Library()
        when:
        def result = lib.someLibraryMethod()
        then:
        result == true
    }
}
