<xsl:stylesheet xmlns:marc="http://www.loc.gov/MARC21/slim" 
                xmlns:oai="http://www.openarchives.org/OAI/2.0/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:exts="java://cz.inovatika.vdk.xml.XSLFunctions" 
                xmlns:nkf="java://cz.inovatika.vdk.common.NKF" 
                xmlns:slouceni="java://cz.inovatika.vdk.common.Slouceni" 
                version="1.1" exclude-result-prefixes="marc oai" extension-element-prefixes="nkf">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="yes" />
    <xsl:param name="uniqueCode" select="''" />
    <xsl:param name="codeType" select="''" />
    <xsl:param name="bohemika" select="''" />
    <xsl:param name="sourceXml" select="''" />
    <xsl:variable name="xslfunctions" select="exts:new()" />
    <xsl:variable name="zdroj"><xsl:choose>
      <xsl:when test="starts-with(//oai:record/oai:header/oai:setSpec, 'VKOLOAI')">VKOL</xsl:when>
      <xsl:when test="starts-with(//oai:record/oai:header/oai:setSpec, 'MZK')">MZK</xsl:when>
      <xsl:when test="starts-with(//oai:record/oai:header/oai:setSpec, 'NKC')">UKF</xsl:when>
      <xsl:otherwise><xsl:value-of select="//oai:record/oai:header/oai:setSpec" /></xsl:otherwise>
    </xsl:choose></xsl:variable>
    <xsl:template match="/">
        <xsl:variable name="request" select="//oai:OAI-PMH/oai:request" />
        
            <xsl:for-each select="//marc:record" >
                <xsl:variable name="identifier" select="../../oai:header/oai:identifier" />
                <xsl:variable name="title" select="concat(marc:datafield[@tag=245]/marc:subfield[@code='a'],marc:datafield[@tag=245]/marc:subfield[@code='b'])"/>
                
                
                <doc>
                    <field name="id" ><xsl:value-of select="$identifier"/></field>
                    <field name="code"><xsl:value-of select="$uniqueCode"/></field>
                    <field name="code_type"><xsl:value-of select="$codeType"/></field>
                    <field name="bohemika"><xsl:value-of select="$bohemika"/></field>
                    
                    <field name="ccnb" ><xsl:value-of select="marc:datafield[@tag='015']/marc:subfield[@code='a']"/></field>
                    <field name="isbn" ><xsl:value-of select="marc:datafield[@tag='020']/marc:subfield[@code='a']"/></field>
                    <field name="issn" ><xsl:value-of select="marc:datafield[@tag='022']/marc:subfield[@code='a']"/></field>
                    
                    <field name="zdroj" ><xsl:value-of select="$zdroj"/></field>
                    <field name="xml" ><xsl:value-of select="$sourceXml"/></field>
                    
                    
                    <field name="title" >
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
                </doc>
            </xsl:for-each>
        
    </xsl:template>
    
    
  
</xsl:stylesheet>
