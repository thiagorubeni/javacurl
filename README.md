# javacurl
A kind of cURL written in Java

<h3>Usage:</h3>
<p>
bin/curl [OPTIONS] &lt;URL&gt;
</p>
<ul>
<li>-?,--help: Prints help</li>
<li>-c,--content &lt;arg&gt;: Content to send; overwrites "f"</li>
<li>-debug: Debug mode</li>
<li>-f,--file &lt;arg&gt;: File with content to send</li>
<li>-H &lt;arg&gt;: Header name-value pair parameters ("-Hname=value")</li>
<li>-keystore &lt;arg&gt;: Key store path</li>
<li>-keystorepass &lt;arg&gt;: Key store password</li>
<li>-keystoretype &lt;arg&gt;: Key store type; default is jks</li>
<li>-m,--method &lt;arg&gt;: Method. Default is GET</li>
<li>-s,--status: Print HTTP status code</li>
<li>-truststore &lt;arg&gt;: Trust store path</li>
<li>-truststorepass &lt;arg&gt;: Trust store password</li>
<li>-truststoretype &lt;arg&gt;: Trust store type; default is jks</li>
</ul>

<h3>Build with Maven:</h3>
Use "mvn clean package"
