classes=.classes
CLASSPATH=$(classes):${COUGAAR_INSTALL_PATH}/lib/core.jar:${COUGAAR_INSTALL_PATH}/lib/build.jar:${COUGAAR_INSTALL_PATH}/lib/glm.jar:${COUGAAR_INSTALL_PATH}/lib/planserver.jar
export CLASSPATH

jar=performance.jar
gen=.gen

all: $(gen) $(jar)

$(gen): org/cougaar/lib/quo/performance/assets/*.def
	cd org/cougaar/lib/quo/performance/assets; make
	touch $(gen)

$(jar): org/cougaar/lib/quo/performance/*.java 
	install -d $(classes)
	javac -d $(classes) \
	org/cougaar/lib/quo/performance/*.java \
	org/cougaar/lib/quo/performance/assets/*.java
	jar -cf $(jar) -C $(classes) .




clean:
	rm -f $(jar)

realclean: clean
	rm -rf  $(classes) $(gen)
	cd org/cougaar/lib/quo/performance/assets; make clean







