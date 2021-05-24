<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output method="html"/>
    <!-- Insert the XSLT Stylesheet here -->
    <xsl:template match="vaccination-plan">
        <html>
            <head>
                <title> Vaccination Plan - Vaccines Overview</title>
                <style>
                    body {
                        font-family: Verdana, sans-serif;
                    }
                    table {
                        font-family: Verdana, sans-serif;
                        border-collapse: collapse;
                        width: 31em;
                        border: 1px solid #080808;
                    }

                    tr {
                        border: 2px solid #000000;
                        border-width: 1px 0;
                    }

                    td, th {
                        border: 1px solid #080808;
                        padding: 8px;    
                        vertical-align: top;
                    }

                    tr:hover {background-color: #fdf3fd;}

                    th {
                        padding-top: 2px;
                        border: 1px solid #080808;
                        padding-bottom: 2px;
                        text-align: center;
                        background-color: #6c3b6c;
                        color: white;
                        vertical-align: top;
                    }
                </style>
            </head>
            <body>
                <h1>Overview on vaccine types</h1>
                  <hr/>
                <xsl:apply-templates select="//vaccine-types/vaccine"/>
            </body>
        </html>
    </xsl:template> 
	
	<!-- Insert additional templates here -->

    <xsl:template name="populateTable">
        <xsl:param name="vaccine"/>
        <table>
            <tr>
                <xsl:for-each select="//vaccines/vaccine[@type_ref=$vaccine]/batch">
                    <xsl:variable name="batchId" select="@id"/>
                    <td>
                        <table>
                            <tr>
                                <th>
                                    <xsl:apply-templates select="."/> 
                                </th>
                            </tr>
                            <tr>
                                <td>
                                    <b>Vaccinated People:</b>
                                </td>
                            </tr>
                            <xsl:for-each select="//patients/patient/vaccine[@ref_batch=$batchId]">
                                <xsl:sort select="../@birth_year"/>
                                <tr>
                                    <td>
                                        <xsl:call-template name="patient">
                                            <xsl:with-param name="batch" select="."/>
                                        </xsl:call-template>
                                    </td>
                                </tr>
                            </xsl:for-each>
                            <tr>
                                <td>
                                    Found: <xsl:value-of select="count(//patients/patient[vaccine[@ref_batch=$batchId]])"/> Patient(s)
                                </td>
                            </tr>
                        </table>
                    </td>
                </xsl:for-each>
            </tr>
        </table>
    </xsl:template>

    <xsl:template name="patient">
        <xsl:param name="batch"/>
        <xsl:variable name="birth_year" select="../@birth_year"/>
        

        <b>Name: </b> <xsl:value-of select="../@name"/> 
        <xsl:if test="$birth_year"> ( Age: <xsl:value-of select="year-from-date(current-date()) - $birth_year"/> )
        </xsl:if>
        <br/>
        <b>Residences(s):</b> <br/>
        <xsl:value-of select="../residences/main"/> <br/>
        <xsl:for-each select="../residences/second">
            <xsl:value-of select="."/> <br/>
        </xsl:for-each>
        <b>Risk Group: </b> <em><xsl:value-of select="../risk-group"/></em> <br/>
        <b>Received doses</b> <br/>

        <xsl:variable name="curVacc" select="following-sibling::vaccination-date[1]/@date"/>
        <xsl:variable name="precCnt" select="count(preceding-sibling::vaccination-date)"/>

        <!-- TODO: implement 1st and 2nd dose counting -->
        <xsl:value-of select="$precCnt + 1"/>.
        dose received on <xsl:value-of select="$curVacc"/>.

        <!-- output name attribute and age of patient (root), year-from-date(current-date()) -->
        
    </xsl:template>

    <xsl:template match="vaccine">
        <h2><xsl:value-of select="name"/></h2>
        <b> Type: </b> 
        <xsl:value-of select="type"/> <br/>

        <xsl:choose>
            <xsl:when test="authorized = 'true'">
                This vaccine is authorized.<br/>
            </xsl:when>
            <xsl:otherwise>
                Warning! This vaccine was not yet authorized.
            </xsl:otherwise>
        </xsl:choose>
        
        <br/>
        <xsl:variable name="vacName" select="name"/>
        <xsl:choose>
            <xsl:when test="count(//vaccines/vaccine[@type_ref=$vacName]) > 0">
                Below is a list of all batches of the vaccine <xsl:value-of select="name"/>. For each such batch, the patients who
                recieved a dose from this batch are listed.  
                <xsl:call-template name="populateTable">
                    <xsl:with-param name="vaccine" select="name"></xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <h4>No batches received so far!</h4>
            </xsl:otherwise>
        </xsl:choose>
        <hr/>
    </xsl:template>

    <xsl:template match="batch">
        <h3>Batch: <xsl:value-of select="@id"/></h3>
        <xsl:value-of select="."/>
    </xsl:template>

</xsl:stylesheet>
