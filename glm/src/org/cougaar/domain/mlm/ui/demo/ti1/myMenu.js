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
var treeMenuRoot       = "ALP Navi-Menu";     // Text for the menu root.
var treeMenuFolders    = 0;               // Sets display of '+' and '-' icons.
var treeMenuAltText    = true;            // Use menu item text for icon image ALT text.

// Define the items for the top-level of the tree menu.


treeMenu.addItem(new TreeMenuItem("Society Views"));
treeMenu.addItem(new TreeMenuItem("Alpine Web Site", "https://www.alpine.bbn.com/", "_blank"));

// Society Views submenu.

var sv = new TreeMenu();
sv.addItem(new TreeMenuItem("Tops"));
sv.addItem(new TreeMenuItem("ICIS"));
sv.addItem(new TreeMenuItem("SRA"));
treeMenu.items[0].makeSubmenu(sv);

// Tops Views sub-submenu.

var topsViews = new TreeMenu();
topsViews.addItem(new TreeMenuItem("Clusters Table", "http://whitney.alpine:5555/alpine/demo/CLUSTERS.PSP", "mainFrame", "alpine-logo-icon.gif"));
topsViews.addItem(new TreeMenuItem("Cluster Relationships Table", "http://whitney.alpine:5555/alpine/demo/CLUSTERS_R.PSP?MODE=1?HTML", "mainFrame", "alpine-logo-icon.gif"));
topsViews.addItem(new TreeMenuItem("Task Browser", "http://whitney.alpine:5555/alpine/demo/TASKS.PSP", "_blank", "alpine-logo-icon.gif"));
//topsViews.addItem(new TreeMenuItem("TPFDD Display", "http://ncombs.bbn.com:5555/alpine/demo/TASKS.PSP", "_blank", "alpine-logo-icon.gif"));
//topsViews.addItem(new TreeMenuItem("Rainbow Asset Utilization Display", "http://ncombs.bbn.com:5555/alpine/demo/TASKS.PSP", "_blank", "alpine-logo-icon.gif"));
//topsViews.addItem(new TreeMenuItem("Unit Assets on Ships", "http://ncombs.bbn.com:5555/alpine/demo/TASKS.PSP", "_blank", "alpine-logo-icon.gif"));
//topsViews.addItem(new TreeMenuItem("Map Display", "omapplet.html", "_blank", "alpine-logo-icon.gif"));
topsViews.addItem(new TreeMenuItem("Map Display", "http://matterhorn.alpine.bbn.com/~demo/demo-19991008/alpine/demo/omapplet.html", "_blank", "alpine-logo-icon.gif"));
topsViews.addItem(new TreeMenuItem("Organization Views", "http://whitney.alpine:5555/alpine/demo/Appjmap.html", "_blank", "alpine-logo-icon.gif"));
sv.items[0].makeSubmenu(topsViews);

// ICIS Views sub-submenu.

var icisViews = new TreeMenu();
icisViews.addItem(new TreeMenuItem("Clusters Table", "http://matterhorn.alpine:5555/alpine/demo/CLUSTERS.PSP", "mainFrame", "alpine-logo-icon.gif"));
icisViews.addItem(new TreeMenuItem("Cluster Relationships Table", "http://matterhorn.alpine:5555/alpine/demo/CLUSTERS_R.PSP?MODE=1?HTML", "mainFrame", "alpine-logo-icon.gif"));
icisViews.addItem(new TreeMenuItem("Task Browser", "http://matterhorn.alpine:5555/alpine/demo/TASKS.PSP", "_blank", "alpine-logo-icon.gif"));
icisViews.addItem(new TreeMenuItem("Status of Supplies", "http://matterhorn.alpine:5555/alpine/demo/ICIS_Supplies.html", "_blank", "alpine-logo-icon.gif"));
//icisViews.addItem(new TreeMenuItem("Status of Repair Parts", "http://ncombs.bbn.com:5555/alpine/demo/TASKS.PSP", "_blank", "alpine-logo-icon.gif"));
sv.items[1].makeSubmenu(icisViews);

// SRA Views sub-submenu.

var sraViews = new TreeMenu();
sraViews.addItem(new TreeMenuItem("Clusters Table", "http://mt_blanc.alpine:5555/alpine/demo/CLUSTERS.PSP", "mainFrame", "alpine-logo-icon.gif"));
sraViews.addItem(new TreeMenuItem("Cluster Relationships Table", "http://mt_blanc.alpine:5555/alpine/demo/CLUSTERS_R.PSP?MODE=1?HTML", "mainFrame", "alpine-logo-icon.gif"));
sraViews.addItem(new TreeMenuItem("Task Browser", "http://mt_blanc.alpine:5555/alpine/demo/TASKS.PSP", "_blank", "alpine-logo-icon.gif"));
//sraViews.addItem(new TreeMenuItem("Asset Utilization Graph", "http://ncombs.bbn.com:5555/alpine/demo/TASKS.PSP", "_blank", "alpine-logo-icon.gif"));
//sraViews.addItem(new TreeMenuItem("Status of Maintenance", "http://ncombs.bbn.com:5555/alpine/demo/TASKS.PSP", "_blank", "alpine-logo-icon.gif"));
sraViews.addItem(new TreeMenuItem("Organization Views", "http://mt_blanc.alpine:5555/alpine/demo/Appjmap.html", "_blank", "alpine-logo-icon.gif"));
sv.items[2].makeSubmenu(sraViews);


