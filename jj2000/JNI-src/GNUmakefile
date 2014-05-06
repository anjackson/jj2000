# CVS identifier:
# $Id: GNUmakefile,v 1.1 2000/06/08 14:58:25 dsanta Exp $
#
#    JJ2000
#
# Makefile to make the shared libraries with all JNI implementations
#
#
# Created by Diego Santa Cruz, 07-06-2000
#

#
# This makefile will compile the native optional components of JJ2000
# into a shared library that can be loaded by the Java runtime. Currently
# it only applies to UNIX machines.
#
# There are already some systems that are configured. The syetm is
# automatically detected in most cases, but some adjustment could be
# required. See below.
#
# See the README file of JJ2000 on how to access the features provided by
# this shared library. The name of the library is 'libjj2000.so'
#
# This makefile requires GNU make. In some systems the GNU make command
# is 'gmake' instead of 'make.
#

# The OS for which we are building should be one of:
# Linux, IRIX, IRIX64 and SunOS (i.e. solaris).
# For other OSes please add the configuration to this file (see below)
# Try to automatically find the OS name
SYSTEM = $(shell uname)

# Try to automatically find the java installation directory
# if this does not work please comment the line below and define the variable
# JAVA_HOME manually or in the make command line
JAVA_HOME := $(dir $(shell which java))..
# Manual definition if the above fails (replace by appropriate directory)
#JAVA_HOME = /usr/java/

# The directory where the compiled library should be placed
LIB_DIR = .

# Set the classpath to find JNI classes. Only necessary to regenerate
# the JNI headers for the JJ2000 classes
CLASSPATH := ../src/:$(CLASSPATH)
export CLASSPATH

# The directory where the JNI headers of JJ2000 should be placed
# (do not change if your are not regenerating the headers)
JJ_JNI_HDR_DIR = .

#
# General config options: add whatever is needed
#

# Some general config options
EXTRA_INCLUDES =
C_OPT = -O

# Config options to generate the headers
JAVAH = javah
JAVAHFLAGS = -jni

#
# System specific config. Modify as needed or add new systems
#

# Linux
ifeq ($(SYSTEM),Linux)
	CC = gcc
	JNI_OS_INCLUDE = -I$(JAVA_HOME)/include/linux
	CFLAGS = -D_REENTRANT $(C_OPT) $(INCLUDES)
	LD = gcc
	LDFLAGS = -shared
endif

# IRIX
# We use the N32 ABI, which is the default for Java Virtual Machine
ifeq ($(SYSTEM),IRIX)
	CC = cc
	JNI_OS_INCLUDE = -I$(JAVA_HOME)/include/irix
	CFLAGS = -n32 -D_POSIX_C_SOURCE=199506L $(C_OPT) $(INCLUDES)
	LD = ld
	LDFLAGS = -n32 -shared
endif

# IRIX64
# In IRIX64 the Java does not use the 64 bit ABI, use N32 ABI.
ifeq ($(SYSTEM),IRIX64)
	CC = cc
	JNI_OS_INCLUDE = -I$(JAVA_HOME)/include/irix
	CFLAGS = -n32 -D_POSIX_C_SOURCE=199506L $(C_OPT) $(INCLUDES)
	LD = ld
	LDFLAGS = -n32 -shared
endif

# Solaris
ifeq ($(SYSTEM),SunOS)
	CC = cc
	JNI_OS_INCLUDE = -I$(JAVA_HOME)/include/solaris
	CFLAGS = -D_POSIX_C_SOURCE=199506L $(C_OPT) $(INCLUDES)
	LD = ld
	LDFLAGS = -G
endif

#
# No modification should be necessary below this line
#

#
# General variable definitions
#

# Includes
JNI_INCLUDE = -I$(JAVA_HOME)/include
INCLUDES = $(EXTRA_INCLUDES) $(JNI_INCLUDE) $(JNI_OS_INCLUDE) \
	-I$(JJ_JNI_HDR_DIR)
# The library name (compiled library will be named libjj2000.so)
LIB_NAME = jj2000

#
# File definitions
#

# The shared library to build
SHLIB = $(LIB_DIR)/lib$(LIB_NAME).so

# The classes using JNI (fully qualified names)
JNI_CLASSES = jj2000.j2k.util.NativeServices

# The C source files to build the library
SOURCES = $(addsuffix .c, $(subst .,_, $(JNI_CLASSES)))

# The objects of compiled C source files
OBJECTS = $(addsuffix .o, $(basename $(SOURCES)))

#
# Target definitions
#

# Default target is to build shared library
default: shared

# The shared library
shared: $(SHLIB)

# How to build the shared library from all object files
$(SHLIB): $(OBJECTS)
	$(LD) $(LDFLAGS) -o $(SHLIB) $(OBJECTS)

# Generic pattern rule to build object files for each source file
%.o : %.c
	$(CC) -c $(CFLAGS) $(CPPFLAGS) $< -o $@

# Generate the JNI headers
headers:
	$(JAVAH) -d $(JJ_JNI_HDR_DIR) $(JAVAHFLAGS) $(JNI_CLASSES)

# Cleanup stuff
clean:
	-rm -f *.o so_locations

# Remove everything
cleanall: clean
	-rm -f $(SHLIB)
