# CVS identifier:
# $Id: GNUmakefile,v 1.3 2002/08/19 12:40:24 grosbois Exp $
#
#    JJ2000
#
# Global makefile. This file should be updated
# only when a new package is added to the project
# tree (see PACKAGELIST). All package targets are 
# generic, and there is usually a special case for 
# the defaut package in the root directory, since
# this package has no name.
#
# Question or comments: henry@crf.canon.fr 
#

include GNUmakefile.var

#List of packages. New packages should to be added here.
PACKAGELIST = \
jj2000.j2k \
jj2000.j2k.util \
jj2000.j2k.io \
jj2000.j2k.image \
jj2000.j2k.image.input \
jj2000.j2k.image.forwcomptransf \
jj2000.j2k.wavelet \
jj2000.j2k.wavelet.analysis \
jj2000.j2k.quantization \
jj2000.j2k.quantization.quantizer \
jj2000.j2k.roi \
jj2000.j2k.roi.encoder \
jj2000.j2k.entropy \
jj2000.j2k.entropy.encoder \
jj2000.j2k.codestream \
jj2000.j2k.codestream.writer \
jj2000.j2k.fileformat \
jj2000.j2k.fileformat.writer \
jj2000.j2k.encoder \
jj2000.j2k.entropy.decoder \
jj2000.j2k.quantization.dequantizer \
jj2000.j2k.wavelet.synthesis \
jj2000.j2k.image.invcomptransf \
jj2000.j2k.image.output \
jj2000.j2k.codestream.reader \
jj2000.j2k.fileformat.reader \
jj2000.j2k.decoder \
jj2000.disp \
gui \
gui.rsa \
rotation \
security \
icc.lut \
icc.tags \
icc.types \
icc \
colorspace.boxes \
colorspace \

#List of packages for the simple applet (SimpleAppletDecoder)
SAPACKAGELIST = \
jj2000.j2k \
jj2000.j2k.util \
jj2000.j2k.io \
jj2000.j2k.image \
jj2000.j2k.image.output \
jj2000.j2k.wavelet \
jj2000.j2k.quantization \
jj2000.j2k.roi \
jj2000.j2k.entropy \
jj2000.j2k.codestream \
jj2000.j2k.fileformat \
jj2000.j2k.entropy.decoder \
jj2000.j2k.quantization.dequantizer \
jj2000.j2k.wavelet.synthesis \
jj2000.j2k.image.invcomptransf \
jj2000.j2k.codestream.reader \
jj2000.j2k.fileformat.reader \
jj2000.j2k.decoder \
jj2000.disp \
gui \
gui.rsa \
rotation \
security \
icc.lut \
icc.tags \
icc.types \
icc \
colorspace.boxes \
colorspace \

#---------------------------Do not edit under this line

#Path to the root directory
RELROOT=.

#Deduce list of package directories
PACKAGEDIRLIST = $(subst .,/,$(PACKAGELIST))

#Deduce list of simple applet package directories
SAPACKAGEDIRLIST = $(subst .,/,$(SAPACKAGELIST))

#Source files of the default package
DEFAULTPACKAGESOURCES = $(wildcard *.java)

#Generate the liste of reference class files in default package
REFERENCEDEFAULTCLASSFILES=$(addsuffix .class, \
$(basename $(DEFAULTPACKAGESOURCES))) 

#Add the release dir to the CLASSPATH
override CLASSPATH := $(shell $(PWDCOMM))/$(RELEASEDIR):$(CLASSPATH)
export CLASSPATH

#
# targets
#

####
# Default target

.PHONY: default
default: files

####
# Compile source code

# -> Force compiling of each package <-
.PHONY: all
all:    $(RELEASEDIR) $(patsubst %,%-makeall,$(PACKAGEDIRLIST)) local

.PHONY: $(patsubst %,%-makeall,$(PACKAGEDIRLIST))
$(patsubst %,%-makeall,$(PACKAGEDIRLIST)):
	$(MAKE) -C $(patsubst %-makeall,%,$@) all

.PHONY: localall
localall:
	$(JAVAC) $(JAVACDEBUGFLAGS) $(wildcard *.java)

# -> Compiles only out-of-date class files <-
.PHONY: files
files:  $(RELEASEDIR) $(patsubst %,%-makefiles,$(PACKAGEDIRLIST)) local

.PHONY: $(patsubst %,%-makefiles,$(PACKAGEDIRLIST))
$(patsubst %,%-makefiles,$(PACKAGEDIRLIST)):
	$(MAKE) -C $(patsubst %-makefiles,%,$@) files

# Target to the default package
.PHONY: local
local:	$(REFERENCEDEFAULTCLASSFILES)

# Rule for building the default package
%.class: %.java
	$(JAVAC) $(JAVACDEBUGFLAGS) $<

# -> Compile a release version <-
.PHONY: release
release: $(RELEASEDIR) $(patsubst %,%-makerel,$(PACKAGEDIRLIST)) localrel

.PHONY: $(patsubst %,%-makerel,$(PACKAGEDIRLIST))
$(patsubst %,%-makerel,$(PACKAGEDIRLIST)):
	$(MAKE) -C $(patsubst %-makerel,%,$@) release
# Erase previous release files and make release directory
.PHONY: $(RELEASEDIR)
$(RELEASEDIR): cleanrel
	$(MKDIR) $(RELEASEDIR)
# Target to the default package for release
.PHONY: localrel
localrel:
	$(JAVAC) $(JAVACRELEASEFLAGS) -d $(RELEASEDIR) $(wildcard *.java)

####
# JAR files

# Compiles a release version and packages it, it then removes the release
# .class files.
.PHONY: distrib
distrib: release $(DISTDIR)
ifdef JJ2000VER
	cd $(RELEASEDIR) ; \
	$(JAR) cf0 $(DISTDIR)/jj2000-$(JJ2000VER).jar . ; \
	$(JAR) cf $(DISTDIR)/jj2000-aplt-$(JJ2000VER).jar \
	$(addsuffix /*.class,$(SAPACKAGEDIRLIST))
else
	cd $(RELEASEDIR) ; \
	$(JAR) cf0 $(DISTDIR)/jj2000.jar . ; \
	$(JAR) cf $(DISTDIR)/jj2000-aplt.jar \
	$(addsuffix /*.class,$(SAPACKAGEDIRLIST))
endif
	$(RECRM) $(RELEASEDIR)/*

$(DISTDIR):
ifneq ($(DISTDIR),$(wildcard $(DISTDIR))) 
	$(MKDIR) $(DISTDIR)
endif

####
# Documentation

# Generate html documentation of jj20000 implementation
.PHONY: doc
doc: 
ifneq ($(DOCDIR),$(wildcard $(DOCDIR))) 
	$(MKDIR) $(DOCDIR)
endif
	$(JAVADOC) $(JAVADOCFLAGS) -d $(DOCDIR) $(PACKAGELIST) \
	$(DEFAULTPACKAGESOURCES)

####
# Cleanup

# Remove *.class files in each package
.PHONY: clean
clean:
	$(foreach DIRECTORY, $(PACKAGEDIRLIST), $(MAKE) -C $(DIRECTORY) clean;)
	$(RM) *.class

# Remove release class files and directory
.PHONY: cleanrel
cleanrel:
	$(RECRM) $(RELEASEDIR)/*

# Remove the JAR files in the distrib directory
.PHONY: cleandist
cleandist:
	$(RECRM) $(DISTDIR)/*

# Call al clean* targets
.PHONY: cleanall
cleanall: clean cleanrel cleandist

####
# Help message

help:
	@echo "" ;
	@echo "    JJ2000 global makefile for GNU make." ;
	@echo "       List of all possible targets" ;
	@echo ""
	@echo "files: (default target)" ;
	@echo "  compiles the packages with one call of the" ;
	@echo "  the compiler per file. File updates are checked" ;
	@echo "  so compiling occurs only when necessary. Debugging" ;
	@echo "  is turned on." ;
	@echo ""
	@echo "distrib:" ;
	@echo "  compiles a release version of the project and creates" ;
	@echo "  a JAR file for distribution and a second one for the" ;
	@echo "  simple applet. The name of the JAR files are " ;
	@echo "  jj2000-JJ2000VER.jar and jj2000-aplt-JJ2000VER.jar (applet)," ;
	@echo "  if the variable JJ2000VER is defined, or jj2000.jar and" ;
	@echo "  jj2000-aplt if not. The JJ2000VER can be defined as a" ;
	@echo "  make variable on the command line (e.g.," ;
	@echo "  make JJ2000VER=1.1 distrib) or as an environment " ;
	@echo "  variable. The JAR files are left in the directory" ;
	@echo "  $(DISTDIR). The compiled .class files are removed" ;
	@echo "  after the JAR file is created." ;
	@echo ""
	@echo "clean:" ;
	@echo "  removes the \"regular\" *.class files from all packages." ;
	@echo ""
	@echo "cleanall:" ;
	@echo "  removes all (regular and release) *.class files as well" ;
	@echo "  as the JAR file (i.e. calls the clean, cleanrel and " ;
	@echo "  cleandist targets)." ;
	@echo ""
	@echo "all:" ;
	@echo "  compiles all packages with a single call to the" ;
	@echo "  compiler per package. It does not check file" ;
	@echo "  updates so compiling is systematic. Debugging is" ;
	@echo "  turned on." ;
	@echo ""
	@echo "doc:" ;
	@echo "  builds the html documentation of the project." ;
	@echo ""
	@echo "release:" ;
	@echo "  builds a release version of the project." ;
	@echo "  This means compiling each package with optimization" ;
	@echo "  turned on and moving the result to the release" ;
	@echo "  directory ($(RELEASEDIR))." ;
	@echo ""
	@echo "local:" ;
	@echo "  compiles only the default package of the project." ;
	@echo ""
	@echo "localrel:" ;
	@echo "  compiles only the default package of the" ;
	@echo "  project in release mode." ;
	@echo ""
	@echo "cleanrel:" ;
	@echo "  removes the files from the release directory" ;
	@echo ""
	@echo "cleandist:" ;
	@echo "  removes the files in the distribution directory" ;
	@echo ""
	@echo "The default target is 'files'." ;
	@echo ""
