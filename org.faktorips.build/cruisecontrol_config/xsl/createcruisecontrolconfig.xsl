<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
    
    <xsl:param name="usecvs"/>
    <xsl:param name="htmlemail"/>
    
    <!-- root node -->
    <xsl:template match="faktoripscruisecontrol">
        <xsl:call-template name="print.header"/>
        <cruisecontrol>
            <!-- print properties -->
            <xsl:apply-templates select="property"/>
            <!-- print project definitions -->
            <xsl:apply-templates select="projects/project"/>
        </cruisecontrol>
    </xsl:template>

    <xsl:template name="print.header">
        <xsl:comment>======================================================================================================</xsl:comment>
        <xsl:comment> W A R N I N G: do not change this file! This file is completely generated </xsl:comment>
        <xsl:comment>          changes must be made in the faktorips.propject.xml configuration file </xsl:comment>
        <xsl:comment>======================================================================================================</xsl:comment>
        <xsl:comment> Cruise Control configuration for all faktorips projects and eclipse features</xsl:comment>
        <xsl:comment> note that this file could be re-generated based on the project definition in </xsl:comment>
        <xsl:comment> 'faktorips.projects.xml', using the ant script './generate.cc.config.xml'</xsl:comment>
        <xsl:comment>======================================================================================================</xsl:comment>    
    </xsl:template>
    
    <!-- print property -->
    <xsl:template match="property">
        <xsl:copy-of select="."/>
    </xsl:template>

    <!-- print project definition -->
    <xsl:template match="project">
        <!-- init variables -->
        <xsl:variable name="projectname"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="cvstag"><xsl:value-of select="@cvstag"/></xsl:variable>
        <xsl:variable name="javaprojectname">
            <xsl:choose>
                <xsl:when test="string-length(@javaprojectname)>0">
                    <xsl:value-of select="@javaprojectname"/>
                </xsl:when>
    		    <xsl:otherwise><xsl:value-of select="@name"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>        
        <xsl:variable name="cvsmodul">
            <xsl:choose>
                <xsl:when test="string-length(@cvsmodul)>0">
                    <xsl:value-of select="@cvsmodul"/>
                </xsl:when>
                <xsl:otherwise><xsl:value-of select="$javaprojectname"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="projectlocation">${workdir}/<xsl:value-of select="$javaprojectname"/></xsl:variable>
        <xsl:variable name="buildfile">
            <xsl:choose>
                <xsl:when test="string-length(@buildfile)>0">
                    <xsl:value-of select="@buildfile"/>
                </xsl:when>
    		    <xsl:otherwise><xsl:value-of select="'build/build.xml'"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="testresultdir">
            <xsl:choose>
                <xsl:when test="string-length(@testresult)>0">
                    <xsl:value-of select="@testresult"/>
                </xsl:when>
    		    <xsl:otherwise><xsl:value-of select="concat($projectlocation,'/build/logs')"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
 
        <!-- start cruisecontrol definitions -->
        <xsl:comment>=====================================================</xsl:comment>
        <xsl:comment>Project definition: <xsl:value-of select="@name"/></xsl:comment>
        <xsl:comment>=====================================================</xsl:comment>
        <project buildafterfailed="false"><xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <!-- listeners -->
            <listeners>
                <currentbuildstatuslistener>
                    <xsl:attribute name="file"><![CDATA[${logdir}/${project.name}/buildStatus.txt]]></xsl:attribute>
                </currentbuildstatuslistener>
            </listeners>
            
            <!-- bootstrappers -->
            <bootstrappers>
                <!-- call clean target -->
                <!-- if using cvs then the clean target will be performed implicitly -->
                <xsl:choose>
                    <xsl:when test="$usecvs!='true'">
                        <antbootstrapper anthome="apache-ant-1.7.0" target="clean">
                            <xsl:attribute name="buildfile"><xsl:value-of select="concat($projectlocation,'/',$buildfile)"/></xsl:attribute>
                        </antbootstrapper>                        
                    </xsl:when>
                </xsl:choose>
            </bootstrappers>
            
            <!-- modificationset cvs or local file system -->
            <modificationset>
            <xsl:for-each select="depends/project">
                <!-- used to trigger a build when another CruiseControl project has a successful build -->
                <buildstatus>
                    <xsl:attribute name="logdir"><![CDATA[${logdir}/]]><xsl:value-of select="@name"/></xsl:attribute>
                </buildstatus>
		    </xsl:for-each>
    		<xsl:choose>
    			<xsl:when test="$usecvs='true'">
                   <!-- cvs default modificationset -->
                   <cvs>
                      <xsl:attribute name="cvsroot"><![CDATA[${cvsroot}]]></xsl:attribute>
                      <xsl:attribute name="module"><xsl:value-of select="$cvsmodul"/></xsl:attribute>
	     			  <xsl:choose>
	     			  	<xsl:when test="@cvstag='HEAD'">
	     			  	  <!-- HEAD not tag attribute necessary -->
				    	</xsl:when>
	     			  	<xsl:when test="string-length(@cvstag)>0">
	                        <xsl:attribute name="tag"><xsl:value-of select="@cvstag"/></xsl:attribute>
				    	</xsl:when>
	                    <xsl:when test='string-length($cvstag)>0'>
	                        <xsl:attribute name="tag"><xsl:value-of select="$cvstag"/></xsl:attribute>
	                    </xsl:when>
	                  </xsl:choose>
                  </cvs>
                  <xsl:for-each select="depends/cvs">
                   <!-- cvs additional modificationset -->
                       <cvs>
                          <xsl:attribute name="cvsroot"><![CDATA[${cvsroot}]]></xsl:attribute>
                          <xsl:attribute name="module"><xsl:value-of select="@module"/></xsl:attribute>
		     			  <xsl:choose>
		     			  	<xsl:when test="@cvstag='HEAD'">
		     			  	  <!-- HEAD not tag attribute necessary -->
					    	</xsl:when>
		     			  	<xsl:when test="string-length(@cvstag)>0">
		                        <xsl:attribute name="tag"><xsl:value-of select="@cvstag"/></xsl:attribute>
					    	</xsl:when>
		                    <xsl:when test='string-length($cvstag)>0'>
		                        <xsl:attribute name="tag"><xsl:value-of select="$cvstag"/></xsl:attribute>
		                    </xsl:when>
		                  </xsl:choose>
                      </cvs>
                  </xsl:for-each>                  
    			  </xsl:when>
    			  <xsl:otherwise>
                  <!-- filesystem modificationset -->
                  <filesystem>
                      <xsl:attribute name="folder"><xsl:value-of select="$projectlocation"/><![CDATA[/src]]></xsl:attribute>
                  </filesystem>
    			</xsl:otherwise>
    		</xsl:choose>
            </modificationset>

            <!-- schedule -->
            <schedule>
               <xsl:attribute name="interval">
                    <xsl:choose>
                        <xsl:when test="string-length(@scheduleintervall)>0">
                            <xsl:value-of select="@scheduleintervall"/>
                        </xsl:when>
                        <xsl:otherwise>60</xsl:otherwise> <!-- 1 minute -->
                    </xsl:choose>               
               </xsl:attribute>
               <!-- ant call -->
    		   <ant uselogger="true" usedebug="false">
                    <xsl:attribute name="buildfile"><![CDATA[${mainbuildfile}]]></xsl:attribute>
                    <xsl:attribute name="target"><xsl:value-of select="@mainbuildtarget"/></xsl:attribute>	  
                    <xsl:if test="count(jvmarg)>0">
                        <xsl:copy-of select="jvmarg"/>
                    </xsl:if>        
                    <xsl:if test="count(property)>0">
                        <xsl:copy-of select="property"/>
                    </xsl:if>        
    		   </ant>
    	    </schedule>
            
            <!-- log -->
            <log>
                <xsl:attribute name="logdir"><![CDATA[${logdir}/${project.name}/]]></xsl:attribute>
                <merge>
                    <xsl:attribute name="dir"><xsl:value-of select="$testresultdir"/></xsl:attribute>
                    <xsl:if test="string-length(@testmerge)>0">
                        <xsl:attribute name="pattern"><xsl:value-of select="@testmerge"/></xsl:attribute>
                    </xsl:if>
                </merge>
            </log>  
            
            <!-- publishers -->
            <publishers>
                <!-- global htmlmail publisher -->
                <xsl:if test="$htmlemail='true'">
                    <htmlemail>
                        <xsl:copy-of select="/faktoripscruisecontrol/publishers/publisher[@id='htmlemail']/child::node()/@*"/>
                        <xsl:copy-of select="/faktoripscruisecontrol/publishers/publisher[@id='htmlemail']/child::node()/*"/>
                        <!-- buildstatus cc projects see http://cruisecontrol.sourceforge.net/main/configxml.html#buildstatus -->
                        <xsl:comment>dummy aliases for buildstatus projects (cc-projectname)</xsl:comment>
                        <xsl:for-each select="/faktoripscruisecontrol/projects/project">
                            <map>
                                <xsl:attribute name="alias">cc-<xsl:value-of select="@name"/></xsl:attribute>
                                <xsl:attribute name="address"></xsl:attribute>
                            </map>
                        </xsl:for-each>
                        <failure>
                            <xsl:attribute name="reportWhenFixed"><xsl:value-of select="'true'"/></xsl:attribute>
                            <xsl:attribute name="address">
                                <xsl:call-template name="all.developeraddress.as.string">
                                    <xsl:with-param name="addresses" select="/faktoripscruisecontrol/publishers/publisher[@id='htmlemail']/child::node()/map" />
                                </xsl:call-template>
                            </xsl:attribute>
                        </failure>
                    </htmlemail>
                </xsl:if>
                <!-- project specific publisher -->
                <xsl:copy-of select="/faktoripscruisecontrol/publishers/publisher[@project=$projectname]/child::node()"/>
            </publishers>
        </project>
    </xsl:template>

    <xsl:template name="all.developeraddress.as.string">
        <xsl:param name="addresses"/>
        <xsl:for-each select="$addresses"><xsl:value-of select="@address"/><xsl:if test="not(position() = count($addresses))">,</xsl:if></xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
