<xsl:stylesheet xmlns:marc="http://www.loc.gov/MARC21/slim" 
                xmlns:oai="http://www.openarchives.org/OAI/2.0/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:exts="java://cz.inovatika.vdk.xml.XSLFunctions" 
                xmlns:nkf="java://cz.inovatika.vdk.common.NKF" 
                xmlns:slouceni="java://cz.inovatika.vdk.common.Slouceni" 
                version="1.1" exclude-result-prefixes="marc oai" extension-element-prefixes="nkf">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="yes" />
    <xsl:param name="file" select="file" />
    <xsl:param name="uniqueCode" select="''" />
    <xsl:param name="codeType" select="''" />
    <xsl:param name="bohemika" select="''" />
    <xsl:variable name="xslfunctions" select="exts:new()" />
    <xsl:variable name="zdroj"><xsl:choose>
      <xsl:when test="starts-with(//oai:record/oai:header/oai:setSpec, 'VKOLOAI')">VKOL</xsl:when>
      <xsl:when test="starts-with(//oai:record/oai:header/oai:setSpec, 'MZK')">MZK</xsl:when>
      <xsl:when test="starts-with(//oai:record/oai:header/oai:setSpec, 'NKC')">UKF</xsl:when>
      <xsl:otherwise><xsl:value-of select="//oai:record/oai:header/oai:setSpec" /></xsl:otherwise>
    </xsl:choose></xsl:variable>
    <xsl:key name="svazek" match="//marc:record/marc:datafield[@tag=996]/marc:subfield[@code='v']" use="." />
    <xsl:template match="/">
        <xsl:variable name="request" select="//oai:OAI-PMH/oai:request" />
        
            <xsl:for-each select="//marc:record" >
                <xsl:variable name="identifier" select="../../oai:header/oai:identifier" />
                <xsl:variable name="title" select="concat(marc:datafield[@tag=245]/marc:subfield[@code='a'],marc:datafield[@tag=245]/marc:subfield[@code='b'])"/>
                <xsl:variable name="autor" select="marc:datafield[@tag=100]/marc:subfield[@code='a']"/>
                <xsl:variable name="mistovydani" select="marc:datafield[@tag=260]/marc:subfield[@code='a']"/>
                <xsl:variable name="vydavatel" select="marc:datafield[@tag=260]/marc:subfield[@code='b']"/>
                <xsl:variable name="datumvydani" select="marc:datafield[@tag=260]/marc:subfield[@code='c']"/>
                
                <xsl:variable name="md5" select="exts:generateNormalizedMD5($xslfunctions, concat($title, $autor, $mistovydani, $vydavatel, $datumvydani))"/>
                
                <doc>
                    <field name="code"><xsl:value-of select="$uniqueCode"/></field>
                    <field name="md5"><xsl:value-of select="$md5"/></field>
                    <field name="bohemika"><xsl:value-of select="$bohemika"/></field>
                    <field name="code_type"><xsl:value-of select="$codeType"/></field>
                    
                    <field name="file" update="add"><xsl:value-of select="$file"/></field>
                    <field name="ccnb" update="add"><xsl:value-of select="marc:datafield[@tag='015']/marc:subfield[@code='a']"/></field>
                    <field name="isbn" update="add"><xsl:value-of select="marc:datafield[@tag='020']/marc:subfield[@code='a']"/></field>
                    <field name="issn" update="add"><xsl:value-of select="marc:datafield[@tag='022']/marc:subfield[@code='a']"/></field>
                    <field name="pocet_doc" update="inc">1</field>
                        
                    <field name="id" update="add"><xsl:value-of select="$identifier"/></field>
                    
                    <xsl:call-template name="pocet_ex" />
                    <xsl:call-template name="ex">
                        <xsl:with-param name="id" select="$identifier" />
                    </xsl:call-template>
                    <field name="oai" update="add"><xsl:value-of select="$request"/></field>
                    
                    
                    <xsl:choose>
                      
		      <xsl:when test="($zdroj='UKF')">
			<xsl:if test="(nkf:hasNKF(marc:datafield[@tag=996]/marc:subfield[@code='c']/text()))">
			  <field name="zdroj" update="add">NKF</field>
			</xsl:if>
			<xsl:if test="(nkf:hasUKF(marc:datafield[@tag=996]/marc:subfield[@code='c']/text()))">
			  <field name="zdroj" update="add">UKF</field>
			</xsl:if>
		      </xsl:when>
		      <xsl:otherwise>
			<field name="zdroj" update="add"><xsl:value-of select="$zdroj"/></field>
		      </xsl:otherwise>
                    </xsl:choose>
                    
                    <field name="titlemd5" update="add"><xsl:value-of select="$title"/></field>
                    <field name="authormd5" update="add"><xsl:value-of select="$autor"/></field>
                    <field name="mistovydani" update="add"><xsl:value-of select="$mistovydani"/></field>
                    <field name="vydavatel" update="add"><xsl:value-of select="$vydavatel"/></field>
                    <field name="datumvydani" update="add"><xsl:value-of select="$datumvydani"/></field>
                    <xsl:variable name="rokvydani" select="exts:validYear($xslfunctions, $datumvydani)" />
                    <xsl:if test="$rokvydani &gt; 0">
                        <field name="rokvydani" update="add">
                            <xsl:value-of select="$rokvydani"/>
                        </field>
                    </xsl:if>  
                    <xsl:call-template name="export" />
                    
                    <field name="format" update="add"><xsl:value-of select="./marc:controlfield[@tag=990]"/></field>
                    <xsl:variable name="leader"><xsl:value-of select="./marc:leader"/></xsl:variable>
                    <field name="leader" update="add"><xsl:value-of select="$leader"/></field>
                    <field name="ldr19" update="add"><xsl:value-of select="substring($leader, 19,1)"/></field>
                    <field name="leader_format" update="add"><xsl:choose>
		      <xsl:when test="not(string(substring($leader, 8, 1)))">invalid</xsl:when>
		      <xsl:when test="substring($leader, 8, 1)=' '">invalid</xsl:when>
		      <xsl:otherwise><xsl:value-of select="substring($leader, 8, 1)"/></xsl:otherwise>
		    </xsl:choose></field>
                    
                    
                    <xsl:for-each select="marc:datafield[@tag=996]/marc:subfield[@code='b']">
                        <field name="carkod" update="add" ><xsl:value-of select="."/></field>
                    </xsl:for-each>
                    <xsl:for-each select="marc:datafield[@tag=996]/marc:subfield[@code='c']">
                        <field name="signatura" update="add" ><xsl:value-of select="."/></field>
                    </xsl:for-each>
                    
                    <field name="title" update="add">
                        <xsl:value-of select="marc:datafield[@tag=245]/marc:subfield[@code='a']"/>
                        <xsl:text>&#160;</xsl:text>
                        <xsl:value-of select="marc:datafield[@tag='245']/marc:subfield[@code='b']"/>
                        <xsl:text>&#160;</xsl:text>
                        <xsl:value-of select="marc:datafield[@tag='245']/marc:subfield[@code='n']"/>
                        <xsl:text>&#160;</xsl:text>
                        <xsl:value-of select="marc:datafield[@tag='245']/marc:subfield[@code='p']"/>
                        <xsl:text>&#160;</xsl:text>
                        <xsl:value-of select="marc:datafield[@tag='245']/marc:subfield[@code='s']"/>
                    </field>
                    <!--
                    <field name="title_sort">
                        <xsl:value-of select="marc:datafield[@tag=245]/marc:subfield[@code='a']"/>
                    </field>
                    -->
                    <field name="title_suggest" update="add"><xsl:value-of select="exts:prepareCzechLower($xslfunctions, $title)"/>##<xsl:value-of select="$title"/></field>
                    <xsl:for-each select="marc:datafield[@tag=246]">
                        <field name="title2" update="add"><xsl:value-of select="./marc:subfield[@code='a']"/></field>
                    </xsl:for-each>
                    <xsl:if test="marc:datafield[@tag=240]/marc:subfield[@code='a']">
                        <field name="title2" update="add"><xsl:value-of select="marc:datafield[@tag=240]/marc:subfield[@code='a']"/></field>
                    </xsl:if>
                    <xsl:if test="marc:datafield[@tag=130]/marc:subfield[@code='a']">
                        <field name="title2" update="add"><xsl:value-of select="marc:datafield[@tag=130]/marc:subfield[@code='a']"/></field>
                    </xsl:if>
					<!--
                    <xsl:for-each select="marc:datafield[@tag=700]">
                        <field name="author" update="add">
                            <xsl:value-of select="./marc:subfield[@code='a']"/>
                        </field>
                    </xsl:for-each>
                    <xsl:if test="marc:datafield[@tag=100]/marc:subfield[@code='a']">
                        <field name="author" update="add"><xsl:value-of select="marc:datafield[@tag=100]/marc:subfield[@code='a']"/></field>
                    </xsl:if>
                    <xsl:if test="marc:datafield[@tag=110]/marc:subfield[@code='a']">
                        <field name="author" update="add">
                            <xsl:value-of select="marc:datafield[@tag=110]/marc:subfield[@code='a']"/>
                        </field>
                    </xsl:if>
                    <xsl:if test="marc:datafield[@tag=111]/marc:subfield[@code='a']">
                        <field name="author" update="add">
                            <xsl:value-of select="marc:datafield[@tag=111]/marc:subfield[@code='a']"/>
                        </field>
                    </xsl:if>
					-->
					
					<field name="author" update="add">
                        <xsl:value-of select="exts:join($xslfunctions, marc:datafield[@tag=100]/marc:subfield[@code='a']/text(), marc:datafield[@tag=110]/marc:subfield[@code='a']/text(), marc:datafield[@tag=111]/marc:subfield[@code='a']/text(), marc:datafield[@tag=700]/marc:subfield[@code='a']/text())"/>
                    </field>
					
					
                    <xsl:for-each select="marc:datafield[@tag=856]/marc:subfield[@code='u']">
                        <field name="url" update="add">
                            <xsl:value-of select="."/>
                        </field>
                    </xsl:for-each>
                    <xsl:for-each select="marc:datafield[@tag=996]/marc:subfield[@code='s']">
                        <field name="status" update="add">
                            <xsl:value-of select="."/>
                        </field>
                    </xsl:for-each>
                    <xsl:for-each select="marc:datafield[@tag=996]/marc:subfield[@code='y']">
                    <xsl:variable name="year" select="exts:validYear($xslfunctions, .)" />
                    <xsl:if test="$year &gt; 0">
                        <field name="rok" update="add">
                            <xsl:value-of select="$year"/>
                        </field>
                    </xsl:if>    
                    </xsl:for-each>
                </doc>
            </xsl:for-each>
        
    </xsl:template>
    
    <xsl:template name="export">
        <xsl:variable name="f100a"><xsl:call-template name="autor_export" /></xsl:variable>
        <xsl:variable name="csv"><xsl:value-of select="slouceni:toCSV(
string(normalize-space(marc:datafield[@tag='020']/marc:subfield[@code='a'])), 
string(normalize-space(marc:datafield[@tag='022']/marc:subfield[@code='a'])), 
string(normalize-space(marc:datafield[@tag='015']/marc:subfield[@code='a'])), 
string(concat(normalize-space(marc:datafield[@tag='245']/marc:subfield[@code='a']),
  normalize-space(marc:datafield[@tag='245']/marc:subfield[@code='b']))),
string(normalize-space(marc:datafield[@tag='245']/marc:subfield[@code='n'])), 
string(normalize-space(marc:datafield[@tag='245']/marc:subfield[@code='p'])), 
string(normalize-space(marc:datafield[@tag='250']/marc:subfield[@code='a'])), 
string(normalize-space($f100a)), 
string(normalize-space(marc:datafield[@tag='110']/marc:subfield[@code='a'])), 
string(normalize-space(marc:datafield[@tag='111']/marc:subfield[@code='a'])), 
string(concat(normalize-space(marc:datafield[@tag='260']/marc:subfield[@code='a']), 
  normalize-space(marc:datafield[@tag='260']/marc:subfield[@code='b']), 
  normalize-space(marc:datafield[@tag='260']/marc:subfield[@code='c']))))" /></xsl:variable>
        <field name="export_json"><xsl:value-of select="slouceni:fromCSV($csv)" /></field>
        <field name="export"><xsl:value-of select="$csv" /></field>
    </xsl:template>
    
    <xsl:template name="autor_export">
        <xsl:choose>
            <xsl:when test="marc:datafield[@tag='100']/marc:subfield[@code='a']">
                <xsl:variable name="f100a"><xsl:value-of select="normalize-space(marc:datafield[@tag='100']/marc:subfield[@code='a'])"/></xsl:variable>
                <xsl:variable name="f100ind"><xsl:value-of select="marc:datafield[@tag='100']/@ind1" /></xsl:variable>
                <xsl:choose>
                    <xsl:when test="marc:datafield[@tag='100']/@ind1 = '1'"><xsl:value-of select="concat(substring-after($f100a, ','), ', ', substring-before($f100a, ','))" /></xsl:when>
                    <xsl:otherwise><xsl:value-of select="$f100a" /></xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="normalize-space(marc:datafield[@tag='245']/marc:subfield[@code='c'])" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
   
    
    <xsl:template name="pocet_ex">
        <xsl:variable name="extag" select="marc:datafield[@tag=996]" />
        <xsl:choose>
            <xsl:when test="marc:datafield[@tag=996]/marc:subfield[@code='v']">
                <xsl:for-each select="marc:datafield[@tag=996][generate-id(marc:subfield[@code='v']) = generate-id(key('svazek', marc:subfield[@code='v'])[1])]">
                    <xsl:variable name="exkey" select="marc:subfield[@code='v']" />
                    <xsl:if test="$exkey!=''">
                    <field name="pocet_exemplaru" update="add">
                        <xsl:value-of select="count($extag/marc:subfield[@code='v'][.=$exkey])"/>
                    </field>
                    <field update="add">
                        <xsl:attribute name="name">pocet_ex_<xsl:value-of select="$zdroj"/></xsl:attribute>
                        <xsl:value-of select="count($extag/marc:subfield[@code='v'][.=$exkey])"/>
                    </field>
                    </xsl:if>
                </xsl:for-each>
                <xsl:variable name="zeroEx" select="count(marc:datafield[@tag=996][not(marc:subfield[@code='v'])])" />
                <xsl:if test="$zeroEx &gt; 0">
                    <field name="pocet_exemplaru" update="add">
                        <xsl:value-of select="$zeroEx"/>
                    </field>
                    <field update="add">
                        <xsl:attribute name="name">pocet_ex_<xsl:value-of select="$zdroj"/></xsl:attribute>
                        <xsl:value-of select="$zeroEx"/>
                    </field>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <field name="pocet_exemplaru" update="add">
                    <xsl:value-of select="count(marc:datafield[@tag=996])"/>
                </field>
                <field update="add">
                    <xsl:attribute name="name">pocet_ex_<xsl:value-of select="$zdroj"/></xsl:attribute>
                    <xsl:value-of select="count(marc:datafield[@tag=996])"/>
                </field>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="ex">
        <xsl:param name="id"/>
        <field name="ex" update="add" >
            {
                "id":"<xsl:value-of select="$id" />",
                "zdroj":"<xsl:value-of select="$zdroj" />",
                "file":"<xsl:value-of select="$file" />",
                "ex":[<xsl:for-each select="marc:datafield[@tag=996]">
                        {
                        <xsl:variable name="signatura"><xsl:call-template name="escape-quot-string">
                        <xsl:with-param name="s" select="./marc:subfield[@code='c']"/>
                        </xsl:call-template></xsl:variable>
                        <xsl:variable name="isNKF">
                            <xsl:choose>
                                <xsl:when test="($zdroj='UKF') and (nkf:isNKF($signatura))">true</xsl:when>
                                <xsl:otherwise>false</xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
			 <xsl:variable name="md5" select="exts:md5FromNodeSet($xslfunctions, ./marc:subfield/text())" />
                        "carkod":"<xsl:value-of select="./marc:subfield[@code='b']"/>",
                        "status":"<xsl:value-of select="./marc:subfield[@code='s']"/>",
                        "signatura":"<xsl:value-of select="$signatura"/>",
                        "isNKF":<xsl:value-of select="$isNKF"/>,
                        "svazek":"<xsl:value-of select="./marc:subfield[@code='v']"/>",
                        "rok":"<xsl:value-of select="./marc:subfield[@code='y']"/>",
                        "dilciKnih":"<xsl:value-of select="./marc:subfield[@code='l']"/>",
                        "cislo":"<xsl:value-of select="./marc:subfield[@code='i']"/>",
                        "md5":"<xsl:value-of select="$md5"/>"
                        }<xsl:if test="position()!=last()">,</xsl:if>
                </xsl:for-each>]
            }
        </field>
    </xsl:template>
    
    
<xsl:template name="escape-bs-string">
    <xsl:param name="s"/>
    <xsl:choose>
      <xsl:when test="contains($s,'\')">
        <xsl:call-template name="escape-quot-string">
          <xsl:with-param name="s" select="concat(substring-before($s,'\'),'\\')"/>
        </xsl:call-template>
        <xsl:call-template name="escape-bs-string">
          <xsl:with-param name="s" select="substring-after($s,'\')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="escape-quot-string">
          <xsl:with-param name="s" select="$s"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Escape the double quote ("). -->
  <xsl:template name="escape-quot-string">
    <xsl:param name="s"/>
    <xsl:choose>
      <xsl:when test="contains($s,'&quot;')">
        <xsl:call-template name="encode-string">
          <xsl:with-param name="s" select="concat(substring-before($s,'&quot;'),'\&quot;')"/>
        </xsl:call-template>
        <xsl:call-template name="escape-quot-string">
          <xsl:with-param name="s" select="substring-after($s,'&quot;')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="encode-string">
          <xsl:with-param name="s" select="$s"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

 <!-- Replace tab, line feed and/or carriage return by its matching escape code. Can't escape backslash
       or double quote here, because they don't replace characters (&#x0; becomes \t), but they prefix 
       characters (\ becomes \\). Besides, backslash should be seperate anyway, because it should be 
       processed first. This function can't do that. -->
  <xsl:template name="encode-string">
    <xsl:param name="s"/>
    <xsl:choose>
      <!-- tab -->
      <xsl:when test="contains($s,'&#x9;')">
        <xsl:call-template name="encode-string">
          <xsl:with-param name="s" select="concat(substring-before($s,'&#x9;'),'\t',substring-after($s,'&#x9;'))"/>
        </xsl:call-template>
      </xsl:when>
      <!-- line feed -->
      <xsl:when test="contains($s,'&#xA;')">
        <xsl:call-template name="encode-string">
          <xsl:with-param name="s" select="concat(substring-before($s,'&#xA;'),'\n',substring-after($s,'&#xA;'))"/>
        </xsl:call-template>
      </xsl:when>
      <!-- carriage return -->
      <xsl:when test="contains($s,'&#xD;')">
        <xsl:call-template name="encode-string">
          <xsl:with-param name="s" select="concat(substring-before($s,'&#xD;'),'\r',substring-after($s,'&#xD;'))"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise><xsl:value-of select="$s"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>
