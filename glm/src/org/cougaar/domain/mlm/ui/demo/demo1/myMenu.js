/******************************************************************************
* This file defines the tree menu with it's items and submenus.               *
******************************************************************************/

// User-defined tree menu data.

var treeMenu           = new TreeMenu();  // This is the main menu.
var treeMenuName       = "myMenu_3.0";    // Make this unique for each tree menu.
var treeMenuDays       = 7;               // Number of days to keep the cookie.
var treeMenuFrame      = "menuFrame";     // Name of the menu frame.
var treeMenuImgDir     = "";              // Path to graphics directory.
var treeMenuBackground = "";              // Background image for menu frame.   
var treeMenuBgColor    = "#ffffff";       // Color for menu frame background.   
var treeMenuFgColor    = "#000000";       // Color for menu item text.
var treeMenuHiBg       = "#008080";       // Color for selected item background.
var treeMenuHiFg       = "#ffffff";       // Color for selected item text.
var treeMenuRoot       = "Site Menu";     // Text for the menu root.
var treeMenuFolders    = 0;               // Sets display of '+' and '-' icons.
var treeMenuAltText    = true;            // Use menu item text for icon image ALT text.

// Define the items for the top-level of the tree menu.

treeMenu.addItem(new TreeMenuItem("Generic HTML/Javascript Palette"));
treeMenu.addItem(new TreeMenuItem("Generic Java Applet Palette"));
treeMenu.addItem(new TreeMenuItem("Scenario Palette"));
treeMenu.addItem(new TreeMenuItem("Ascent Palette"));
treeMenu.addItem(new TreeMenuItem("Maya Palette"));
treeMenu.addItem(new TreeMenuItem("Storyboards"));
treeMenu.addItem(new TreeMenuItem("Alpine Web Site", "https://www.alpine.bbn.com/", "_blank"));
treeMenu.addItem(new TreeMenuItem("Other Resources", "../links.html", "mainFrame"));
treeMenu.addItem(new TreeMenuItem("What's New", "../new.html", "mainFrame"));

// AlpHTMLJSPalette submenu.

var htmljsPal = new TreeMenu();
htmljsPal.addItem(new TreeMenuItem("ALP Widgets"));
htmljsPal.addItem(new TreeMenuItem("References"));
htmljsPal.addItem(new TreeMenuItem("Sites"));
treeMenu.items[0].makeSubmenu(htmljsPal);

// ALP HTML JS Palette COMPONENTS Code Examples sub-submenu.

var htmljsPalComponents = new TreeMenu();
htmljsPalComponents.addItem(new TreeMenuItem("Cluster Relationships Table", "/alpine/demo/CLUSTERS_R.PSP?MODE=1?HTML", "mainFrame", "menu_link_local.gif"));
htmljsPalComponents.addItem(new TreeMenuItem("Task Viewer", "/alpine/demo/TASKS.PSP",  "_blank", "menu_link_local.gif"));
htmljsPalComponents.addItem(new TreeMenuItem("Simple Inventory Graph", "/alpine/demo/INVENTORY.PSP", "mainFrame", "menu_link_local.gif"));
htmljsPalComponents.addItem(new TreeMenuItem("Alert Activation", "/alpine/demo/SelectAlert.html", "mainFrame", "menu_link_local.gif"));
htmljsPalComponents.addItem(new TreeMenuItem("Simple Alerts", "/alpine/demo/AlpAlert.html", "_blank", "menu_link_local.gif"));
htmljsPalComponents.addItem(new TreeMenuItem("Simple Drill-down", "/alpine/demo/CLUSTERS_R.PSP?MODE=1?UNEXPANDED_HTML", "mainFrame", "menu_link_local.gif"));
htmljsPalComponents.addItem(new TreeMenuItem("Multi-field Drill-down: LPS White Pages", "/alpine/demo/CLUSTERS.PSP", "mainFrame", "menu_link_local.gif"));
htmljsPal.items[0].makeSubmenu(htmljsPalComponents);

// DHTML References sub-submenu.

var dhtml_refs = new TreeMenu();
dhtml_refs.addItem(new TreeMenuItem("Netscape: JavaScript Documentation", "http://developer.netscape.com/docs/manuals/index.html", "_blank", "menu_link_ref.gif"));
dhtml_refs.addItem(new TreeMenuItem("Builder.com", "http://builder.cnet.com/Programming/", "_blank", "menu_link_ref.gif"));
//dhtml_refs.addItem(new TreeMenuItem("ScriptSearch", "http://www.scriptsearch.com/", "_blank", "menu_link_ref.gif"));
dhtml_refs.addItem(new TreeMenuItem("Webreference: JavaScript", "http://webreference.com/js/", "_blank", "menu_link_ref.gif"));
htmljsPal.items[1].makeSubmenu(dhtml_refs);

// Generic HTML/JS Sites sub-submenu.

var dhtml_sites = new TreeMenu();
dhtml_sites.addItem(new TreeMenuItem("Tree Menu Source", "http://members.aol.com/MHall75819/JavaScript/tree.html", "_blank"));
htmljsPal.items[2].makeSubmenu(dhtml_sites);

// Generic Java Applet submenu.

var java_applet = new TreeMenu();
java_applet.addItem(new TreeMenuItem("ALP Widgets"));
treeMenu.items[1].makeSubmenu(java_applet);

// Scenario Palette submenu.

var scene_pal = new TreeMenu();
scene_pal.addItem(new TreeMenuItem("Transportation Scenario Components"));
scene_pal.addItem(new TreeMenuItem("Supply Views Components"));
scene_pal.addItem(new TreeMenuItem("Support Views Components"));
scene_pal.addItem(new TreeMenuItem("General Views Components"));
treeMenu.items[2].makeSubmenu(scene_pal);

         // Scenario Palette -- Transportation Scenario submenu
         var trans_scene_pal = new TreeMenu();
         trans_scene_pal.addItem(new TreeMenuItem("Transportation Schedule", "/alpine/demo/scenarios/transportcomponents.html", "mainFrame", "alpine-logo-icon.gif")); 
         scene_pal.items[0].makeSubmenu(trans_scene_pal);


// ALP Java Applet submenu.
var alp_applet = new TreeMenu();
alp_applet.addItem(new TreeMenuItem("Transportation Schedule", "/alpine/demo/schedulechartui.html", "mainFrame", "menu_link_java.gif"));
alp_applet.addItem(new TreeMenuItem("3ID Assets", "/alpine/demo/aggregateassetchartui.html", "mainFrame", "menu_link_java.gif"));
alp_applet.addItem(new TreeMenuItem("MCCGlobalMode Queries", "/alpine/demo/query_example.html", "mainFrame", "menu_link_java.gif"));
alp_applet.addItem(new TreeMenuItem("Tasks and Allocations", "/alpine/demo/stripchart.html", "mainFrame", "menu_link_java.gif"));
alp_applet.addItem(new TreeMenuItem("Assets Transported", "/alpine/demo/drilldown.html", "mainFrame", "menu_link_java.gif"));
alp_applet.addItem(new TreeMenuItem("Inventory", "/alpine/demo/inventorychart.html", "mainFrame", "menu_link_java.gif"));
alp_applet.addItem(new TreeMenuItem("Supply", "/alpine/demo/supplystoplight.html", "mainFrame", "menu_link_java.gif"));
alp_applet.addItem(new TreeMenuItem("Policy Editor", "/alpine/demo/Policy.html", "mainFrame", "menu_link_java.gif"));
alp_applet.addItem(new TreeMenuItem("ALP Clusters", "/alpine/demo/AlpMapDemo/Appjmap.html", "mainFrame", "menu_link_java.gif"));
alp_applet.addItem(new TreeMenuItem("Spatial Index View", "/alpine/demo/GeoView.html", "mainFrame", "menu_link_java.gif"));
alp_applet.addItem(new TreeMenuItem("Spatial Index View with sprites", "/alpine/demo/GeoView2.html", "mainFrame", "menu_link_java.gif"));

java_applet.items[0].makeSubmenu(alp_applet);

// ASCENT submenu.

var ascent = new TreeMenu();
//ascent.addItem(new TreeMenuItem("Ascent Widgets"));
ascent.addItem(new TreeMenuItem("Ascent Log Plan View", "/alpine/demo/AscentSnapshot.html", "mainFrame", "menu_link_local.gif"));
ascent.addItem(new TreeMenuItem("References"));
ascent.addItem(new TreeMenuItem("Sites"));
treeMenu.items[3].makeSubmenu(ascent);


// MAYA submenu.

var maya = new TreeMenu();
maya.addItem(new TreeMenuItem("Maya Widgets"));
maya.addItem(new TreeMenuItem("References"));
maya.addItem(new TreeMenuItem("Sites"));
treeMenu.items[4].makeSubmenu(maya);


// STORYBOARD submenu.

var storyboard = new TreeMenu();

// operator chooses perturbation scenario and kicks it off
storyboard.addItem(new TreeMenuItem("Perturbation Start", "/alpine/demo/KickOffPerturbation.html", "mainFrame", "menu_link_local.gif"));

// browser alert; operator notes supply alert
storyboard.addItem(new TreeMenuItem("Browser Alert", "/alpine/demo/browseralertsnapshot.html", "mainFrame", "menu_link_local.gif"));

// inventory display; note that stockage levels are low
storyboard.addItem(new TreeMenuItem("Stockage Level", "/alpine/demo/stockagelow.html", "mainFrame", "menu_link_local.gif"));

// policy menu; choose to edit stockage level polciy
storyboard.addItem(new TreeMenuItem("Policy Menu", "/alpine/demo/PolicyMenu.html", "mainFrame", "menu_link_local.gif"));

// stockage level policy display
storyboard.addItem(new TreeMenuItem("Stockage Level Policy", "/alpine/demo/PolicySnapshot.html", "mainFrame", "menu_link_local.gif"));

// inventory display; note that stockage levels are now acceptable
storyboard.addItem(new TreeMenuItem("Stockage Level", "/alpine/demo/stockageok.html", "mainFrame", "menu_link_local.gif"));

//storyboard.addItem(new TreeMenuItem("Policy Editor", "/alpine/demo/Policy.html", "_blank", "menu_link_java.gif"));
treeMenu.items[5].makeSubmenu(storyboard);


// JavaScript Code Examples sub-submenu.

//var js_ex = new TreeMenu();
//js_ex.addItem(new TreeMenuItem("Altering History", "history.html", "_blank"));
//js_ex.addItem(new TreeMenuItem("Capturing Events", "dimensions.html", "_blank"));
//js_ex.addItem(new TreeMenuItem("Drop-down Menu", "dropdown.html", "_blank"));
//js_ex.addItem(new TreeMenuItem("Dynamic Form Updating", "playoffs.html", "_blank"));
//js_ex.addItem(new TreeMenuItem("Find in Page", "find.html", "_blank"));
//js_ex.addItem(new TreeMenuItem("Form Saver", "formsaver.html", "_blank"));
//js_ex.addItem(new TreeMenuItem("Form Validation", "weekly.html", "_blank"));
//js_ex.addItem(new TreeMenuItem("Highlighted Menu Buttons", "menu.html", "_blank"));
//js_ex.addItem(new TreeMenuItem("Image Loading", "loading.html", "_blank"));
//js_ex.addItem(new TreeMenuItem("Jukebox", "jukebox.html", "_blank"));
//js_ex.addItem(new TreeMenuItem("Pop-up Scoreboard", "pop-up.html", "_blank"));
//js_ex.addItem(new TreeMenuItem("Progress Bar", "progress.html", "_blank"));
//js_ex.addItem(new TreeMenuItem("Shopping Cart", "shoppingcart.html", "_blank"));
//js_ex.addItem(new TreeMenuItem("Using Cookies", "cookie.html", "_blank"));
//js_ex.addItem(new TreeMenuItem("Window Sizing", "windowsize.html", "_blank"));
//js_ex.addItem(new TreeMenuItem("Year 2000 Countdown", "countdown.html", "_blank"));
//htmljsPal.items[3].makeSubmenu(js_ex);


// ASCENT References sub-submenu.

var js_refs = new TreeMenu();
js_refs.addItem(new TreeMenuItem("Webreference: JavaScript", "http://webreference.com/js/", "_blank", "menu_link_ref.gif"));
ascent.items[1].makeSubmenu(js_refs);

// ASCENT Sites sub-submenu.

var js_sites = new TreeMenu();
//js_sites.addItem(new TreeMenuItem("Ascent", "http://www.ascent.com", "_blank"));
ascent.items[2].makeSubmenu(js_sites);


// MAYA References sub-submenu.

var js_refs = new TreeMenu();
//js_refs.addItem(new TreeMenuItem("Webreference: JavaScript", "http://webreference.com/js/", "_blank", "menu_link_ref.gif"));
maya.items[1].makeSubmenu(js_refs);

// MAYA Sites sub-submenu.

var js_sites = new TreeMenu();
//js_sites.addItem(new TreeMenuItem("Maya", "http://www.maya.com", "_blank"));
maya.items[2].makeSubmenu(js_sites);


// STORYBOARD PERTURBATION 1 sub-submenu.

//var story1 = new TreeMenu();
//story1.addItem(new TreeMenuItem("Maya", "http://www.maya.com", "_blank"));
//storyboard.items[0].makeSubmenu(story1);

