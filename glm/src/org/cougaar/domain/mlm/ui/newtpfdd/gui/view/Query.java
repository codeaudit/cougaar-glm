package org.cougaar.domain.mlm.ui.newtpfdd.gui.view;

import java.sql.Connection;

public interface Query {
  QueryResponse getResponse (Connection connection, int connectionType);
}