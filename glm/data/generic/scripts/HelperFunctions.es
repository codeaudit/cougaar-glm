UtilPackage  = Packages.java.util;
function arrayToVector( obj )  {
 	myVector = new UtilPackage.Vector();
 	for ( i=0; i < obj.length; i++ ) {
 		myVector.addElement(obj[i] );
 	}	
	writeln("hello from getArray");
	return( myVector );
}

function vectorToArray( obj ) {
	arr = new Array();
	myEnum = obj.elements();
	i=0;
	while ( myEnum.hasMoreElements() ) {
		arr[i] = myEnum.nextElement();
		i++;
	}

	return arr;
}

function getClusterObjectFactory() {
	return cof;
}

function wake() {
	_plugin.wake();
}

function publishAdd( obj ) {
	_plugin.getPluginSubscriber().publishAdd( obj );
}

function publishChange( obj ) {
	_plugin.getPluginSubscriber().publishChange( obj );
}

function publishRemove( obj ) {
	_plugin.getPluginSubscriber().publishRemove( obj );
}

function getSubscriber() {
	return _plugin.getPluginSubscriber();
}
