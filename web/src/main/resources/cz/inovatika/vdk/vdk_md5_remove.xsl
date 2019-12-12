<xsl:stylesheet xmlns:marc="http://www.loc.gov/MARC21/slim" 
                xmlns:oai="http://www.openarchives.org/OAI/2.0/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:exts="java://cz.inovatika.vdk.xml.XSLFunctions" 
                xmlns:nkf="java://cz.inovatika.vdk.common.NKF" 
                xmlns:slouceni="java://cz.inovatika.vdk.common.Slouceni" 
                version="1.1" exclude-result-prefixes="marc oai" extension-element-prefixes="nkf">
  <xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="yes" />
  <xsl:param name="uniqueCode" select="''" />
  <xsl:variable name="xslfunctions" select="exts:new()" />
  <xsl:variable name="zdroj">
    <xsl:choose>
      <xsl:when test="starts-with(//oai:record/oai:header/oai:setSpec, 'VKOLOAI')">VKOL</xsl:when>
      <xsl:when test="starts-with(//oai:record/oai:header/oai:setSpec, 'MZK')">MZK</xsl:when>
      <xsl:when test="starts-with(//oai:record/oai:header/oai:setSpec, 'NKC')">UKF</xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="//oai:record/oai:header/oai:setSpec" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:key name="svazek" match="//marc:record/marc:datafield[@tag=996]/marc:subfield[@code='v']" use="." />
  <xsl:template match="/">
        
    <xsl:for-each select="//marc:record" >
      <xsl:variable name="identifier" select="../../oai:header/oai:identifier" />
      <xsl:variable name="title" select="concat(marc:datafield[@tag=245]/marc:subfield[@code='a'],marc:datafield[@tag=245]/marc:subfield[@code='b'])"/>
      <xsl:variable name="autor" select="marc:datafield[@tag=100]/marc:subfield[@code='a']"/>
      <xsl:variable name="mistovydani" select="marc:datafield[@tag=260]/marc:subfield[@code='a']"/>
      <xsl:variable name="vydavatel" select="marc:datafield[@tag=260]/marc:subfield[@code='b']"/>
      <xsl:variable name="datumvydani" select="marc:datafield[@tag=260]/marc:subfield[@code='c']"/>
      <xsl:variable name="md5" select="exts:generateNormalizedMD5($xslfunctions, concat($title, $autor, $mistovydani, $vydavatel, $datumvydani))"/>
                
      <doc>
        <field name="code">
          <xsl:value-of select="$uniqueCode"/>
        </field>
        <field name="md5">
          <xsl:value-of select="$md5"/>
        </field>
                    
        <field name="ccnb" update="removeregex">
          <xsl:value-of select="marc:datafield[@tag='015']/marc:subfield[@code='a']"/>
        </field>
        <field name="isbn" update="removeregex">
          <xsl:value-of select="exts:escapeRegex($xslfunctions, marc:datafield[@tag='020']/marc:subfield[@code='a'])"/>
        </field>
        <field name="issn" update="removeregex">
          <xsl:value-of select="exts:escapeRegex($xslfunctions, marc:datafield[@tag='022']/marc:subfield[@code='a'])"/>
        </field>
        <xsl:call-template name="pocet_ex" />
                    
        <xsl:choose>
                      
          <xsl:when test="($zdroj='UKF')">
            <xsl:if test="(nkf:hasNKF(marc:datafield[@tag=996]/marc:subfield[@code='c']/text()))">
              <field name="zdroj" update="removeregex">NKF</field>
            </xsl:if>
            <xsl:if test="(nkf:hasUKF(marc:datafield[@tag=996]/marc:subfield[@code='c']/text()))">
              <field name="zdroj" update="removeregex">UKF</field>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <field name="zdroj" update="removeregex">
              <xsl:value-of select="$zdroj"/>
            </field>
          </xsl:otherwise>
        </xsl:choose>
                    
        <field name="titlemd5" update="removeregex">
          <xsl:value-of select="exts:escapeRegex($xslfunctions, $title)"/>
        </field>
        <field name="authormd5" update="removeregex">
          <xsl:value-of select="exts:escapeRegex($xslfunctions, $autor)"/>
        </field>
        <field name="mistovydani" update="removeregex">
          <xsl:value-of select="exts:escapeRegex($xslfunctions, $mistovydani)"/>
        </field>
        <field name="vydavatel" update="removeregex">
          <xsl:value-of select="exts:escapeRegex($xslfunctions, $vydavatel)"/>
        </field>
        <field name="datumvydani" update="removeregex">
          <xsl:value-of select="exts:escapeRegex($xslfunctions, $datumvydani)"/>
        </field>
        <xsl:variable name="rokvydani" select="exts:validYear($xslfunctions, $datumvydani)" />
        <xsl:if test="$rokvydani &gt; 0">
          <field name="rokvydani" update="removeregex">
            <xsl:value-of select="exts:escapeRegex($xslfunctions, $rokvydani)"/>
          </field>
        </xsl:if>
                    
        <field name="format" update="removeregex">
          <xsl:value-of select="./marc:controlfield[@tag=990]"/>
        </field>
        <xsl:variable name="leader">
          <xsl:value-of select="./marc:leader"/>
        </xsl:variable>
        <field name="leader" update="removeregex">
          <xsl:value-of select="$leader"/>
        </field>
        <field name="ldr19" update="removeregex">
          <xsl:value-of select="substring($leader, 19,1)"/>
        </field>
        <field name="leader_format" update="removeregex">
          <xsl:choose>
            <xsl:when test="not(string(substring($leader, 8, 1)))">invalid</xsl:when>
            <xsl:when test="substring($leader, 8, 1)=' '">invalid</xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="substring($leader, 8, 1)"/>
            </xsl:otherwise>
          </xsl:choose>
        </field>
                    
                    
        <xsl:for-each select="marc:datafield[@tag=996]/marc:subfield[@code='b']">
          <field name="carkod" update="removeregex" >
            <xsl:value-of select="."/>
          </field>
        </xsl:for-each>
        <xsl:for-each select="marc:datafield[@tag=996]/marc:subfield[@code='c']">
          <field name="signatura" update="removeregex" >
            <xsl:value-of select="exts:escapeRegex($xslfunctions, .)"/>
          </field>
        </xsl:for-each>
        <xsl:variable name="title">            
          <xsl:value-of select="marc:datafield[@tag=245]/marc:subfield[@code='a']"/>
          <xsl:text>&#160;</xsl:text>
          <xsl:value-of select="marc:datafield[@tag='245']/marc:subfield[@code='b']"/>
          <xsl:text>&#160;</xsl:text>
          <xsl:value-of select="marc:datafield[@tag='245']/marc:subfield[@code='n']"/>
          <xsl:text>&#160;</xsl:text>
          <xsl:value-of select="marc:datafield[@tag='245']/marc:subfield[@code='p']"/>
          <xsl:text>&#160;</xsl:text>
          <xsl:value-of select="marc:datafield[@tag='245']/marc:subfield[@code='s']"/>
        </xsl:variable>
        <field name="title" update="removeregex">
          <xsl:value-of select="exts:escapeRegex($xslfunctions, $title)"/>
        </field>
        <!--
        <field name="title_sort">
            <xsl:value-of select="marc:datafield[@tag=245]/marc:subfield[@code='a']"/>
        </field>
        -->
        <field name="title_suggest" update="removeregex">
          <xsl:value-of select="exts:escapeRegex($xslfunctions, exts:prepareCzechLower($xslfunctions, $title))"/>##<xsl:value-of select="exts:escapeRegex($xslfunctions, $title)"/>
        </field>
        <xsl:for-each select="marc:datafield[@tag=246]">
          <field name="title2" update="removeregex">
            <xsl:value-of select="exts:escapeRegex($xslfunctions, ./marc:subfield[@code='a'])"/>
          </field>
        </xsl:for-each>
        <xsl:if test="marc:datafield[@tag=240]/marc:subfield[@code='a']">
          <field name="title2" update="removeregex">
            <xsl:value-of select="exts:escapeRegex($xslfunctions, marc:datafield[@tag=240]/marc:subfield[@code='a'])"/>
          </field>
        </xsl:if>
        <xsl:if test="marc:datafield[@tag=130]/marc:subfield[@code='a']">
          <field name="title2" update="removeregex">
            <xsl:value-of select="exts:escapeRegex($xslfunctions, marc:datafield[@tag=130]/marc:subfield[@code='a'])"/>
          </field>
        </xsl:if>
        
        <xsl:variable name="author" select="exts:join($xslfunctions, marc:datafield[@tag=100]/marc:subfield[@code='a']/text(), marc:datafield[@tag=110]/marc:subfield[@code='a']/text(), marc:datafield[@tag=111]/marc:subfield[@code='a']/text(), marc:datafield[@tag=700]/marc:subfield[@code='a']/text())"/>
        <xsl:if test="$author != ''">
          <field name="author" update="removeregex">.<xsl:value-of select="exts:escapeRegex($xslfunctions, $author)" />.</field>
        </xsl:if>
					
        <xsl:for-each select="marc:datafield[@tag=856]/marc:subfield[@code='u']">
          <field name="url" update="removeregex">
            <xsl:value-of select="exts:escapeRegex($xslfunctions, .)"/>
          </field>
        </xsl:for-each>
        <xsl:for-each select="marc:datafield[@tag=996]/marc:subfield[@code='s']">
          <field name="status" update="removeregex">
            <xsl:value-of select="exts:escapeRegex($xslfunctions, .)"/>
          </field>
        </xsl:for-each>
        <xsl:for-each select="marc:datafield[@tag=996]/marc:subfield[@code='y']">
          <xsl:variable name="year" select="exts:validYear($xslfunctions, .)" />
          <xsl:if test="$year &gt; 0">
            <field name="rok" update="removeregex">
              <xsl:value-of select="exts:escapeRegex($xslfunctions, $year)"/>
            </field>
          </xsl:if>    
        </xsl:for-each>
      </doc>
    </xsl:for-each>
        
  </xsl:template>
   
    
  <xsl:template name="pocet_ex">
    <xsl:variable name="extag" select="marc:datafield[@tag=996]" />
    <xsl:choose>
      <xsl:when test="marc:datafield[@tag=996]/marc:subfield[@code='v']">
        <xsl:for-each select="marc:datafield[@tag=996][generate-id(marc:subfield[@code='v']) = generate-id(key('svazek', marc:subfield[@code='v'])[1])]">
          <xsl:variable name="exkey" select="marc:subfield[@code='v']" />
          <xsl:if test="$exkey!=''">
            <field name="pocet_exemplaru" update="removeregex">
              <xsl:value-of select="count($extag/marc:subfield[@code='v'][.=$exkey])"/>
            </field>
            <field update="removeregex">
              <xsl:attribute name="name">pocet_ex_<xsl:value-of select="$zdroj"/></xsl:attribute>
              <xsl:value-of select="count($extag/marc:subfield[@code='v'][.=$exkey])"/>
            </field>
          </xsl:if>
        </xsl:for-each>
        <xsl:variable name="zeroEx" select="count(marc:datafield[@tag=996][not(marc:subfield[@code='v'])])" />
        <xsl:if test="$zeroEx &gt; 0">
          <field name="pocet_exemplaru" update="removeregex">
            <xsl:value-of select="$zeroEx"/>
          </field>
          <field update="removeregex">
            <xsl:attribute name="name">pocet_ex_<xsl:value-of select="$zdroj"/></xsl:attribute>
            <xsl:value-of select="$zeroEx"/>
          </field>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <field name="pocet_exemplaru" update="removeregex">
          <xsl:value-of select="count(marc:datafield[@tag=996])"/>
        </field>
                
      </xsl:otherwise>
    </xsl:choose>
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
      <xsl:otherwise>
        <xsl:value-of select="$s"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>
