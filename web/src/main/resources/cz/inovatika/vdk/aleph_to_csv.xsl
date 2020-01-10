<xsl:stylesheet xmlns:marc="http://www.loc.gov/MARC21/slim" 
                xmlns:oai="http://www.openarchives.org/OAI/2.0/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:exts="java://cz.incad.xsl.XSLFunctions" 
                xmlns:nkf="java://cz.incad.vdkcommon.NKF" 
                xmlns:slouceni="java://cz.incad.vdkcommon.Slouceni" 
                version="1.1" exclude-result-prefixes="marc oai" extension-element-prefixes="nkf">
    <xsl:output method="text" indent="yes" encoding="UTF-8" omit-xml-declaration="yes" />
    
    <xsl:variable name="xslfunctions" select="exts:new()" />
    <xsl:template match="/">
        <xsl:variable name="request" select="//oai:OAI-PMH/oai:request" />
        
            <xsl:for-each select="//marc:record" >
                <xsl:call-template name="export" /><xsl:text>&#xa;</xsl:text>
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
        <xsl:value-of select="$csv" />
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
