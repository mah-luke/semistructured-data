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

    <xsl:template name="populateTable" parameter="vaccine">
        <!-- TODO: implement template 
            iterate over all batch elems whose parent has type_ref attribute 
            matching parameter
        -->
    </xsl:template>

    <xsl:template name="patient" parameter="batch">
        <!-- TODO: implement template -->

    </xsl:template>

    <xsl:template match="vaccine">
        <!-- TODO: implement template -->
        <!-- print head and then type of vaccine + create warning -->
    </xsl:template>

</xsl:stylesheet>
