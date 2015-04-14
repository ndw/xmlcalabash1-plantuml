<p:declare-step version='1.0' name="main"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                exclude-inline-prefixes="c cx">
<p:output port="result"/>

<p:import href="../../../resources/library.xpl"/>

<cx:plantuml>
  <p:input port="source">
    <p:inline><doc>@startuml

class BaseClass

namespace net.dummy #DDDDDD {
    .BaseClass &lt;|-- Person
    Meeting o-- Person

    .BaseClass &lt;|- Meeting
}

namespace net.foo {
  net.dummy.Person  &lt;|- Person
  .BaseClass &lt;|-- Person

  net.dummy.Meeting o-- Person
}

BaseClass &lt;|-- net.unused.Person

@enduml</doc></p:inline>
  </p:input>
</cx:plantuml>

<p:choose>
  <p:when test="contains(string(/), 'data:image/png;')">
    <p:identity>
      <p:input port="source">
        <p:inline><c:result>PASS</c:result></p:inline>
      </p:input>
    </p:identity>
  </p:when>
  <p:otherwise>
    <p:error code="FAIL">
      <p:input port="source">
        <p:inline><message>Did not find expected text.</message></p:inline>
      </p:input>
    </p:error>
  </p:otherwise>
</p:choose>

</p:declare-step>
