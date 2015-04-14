<p:library xmlns:p="http://www.w3.org/ns/xproc"
           xmlns:cx="http://xmlcalabash.com/ns/extensions"
           version="1.0">

<p:declare-step type="cx:plantuml">
   <p:input port="source"/>
   <p:output port="result"/>
   <p:option name="format" select="'png'" cx:type="png|svg"/>
   <p:option name="html" select="false()"/>
</p:declare-step>

</p:library>
