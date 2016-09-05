/*
 * Copyright 2016 Mobicage NV
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @@license_version:1.1@@
 */

package com.mobicage.rogerthat.util.db;

import java.util.ArrayList;
import java.util.List;

public class DbBuilder {

    private static final String INDEX_PREFIX = "ix";
    private static final String INDEX_SEPARATOR = "_";

    private static class DbColumn {
        private String mColumnName;
        private String mColumnType;
        private boolean mIndexed;

        public DbColumn(String pColumnName, String pColumnType, boolean pIndexed) {
            mColumnName = pColumnName;
            mColumnType = pColumnType;
            mIndexed = pIndexed;
        }
    }

    private List<DbColumn> mColumns;
    private String mDbTable;

    public DbBuilder(String pDbTable) {
        mDbTable = pDbTable;
        mColumns = new ArrayList<DbColumn>();
    }

    public void addColumn(String pColumnName, String pColumnType) {
        mColumns.add(new DbColumn(pColumnName, pColumnType, false));
    }

    public void addIndexedColumn(String pColumnName, String pColumnType) {
        mColumns.add(new DbColumn(pColumnName, pColumnType, true));
    }

    public String getCreationSQL() {
        StringBuilder query = new StringBuilder();
        createMainTableSQL(query);
        createIndicesSQL(query);
        return query.toString();
    }

    private void createMainTableSQL(StringBuilder query) {
        query.append("CREATE TABLE IF NOT EXISTS ");
        query.append(mDbTable);
        query.append("(");
        boolean firstEntry = true;
        for (DbColumn column : mColumns) {
            if (!firstEntry) {
                query.append(", ");
            } else {
                firstEntry = false;
            }
            query.append(column.mColumnName);
            query.append(" ");
            query.append(column.mColumnType);
        }
        query.append("); ");
    }

    private void createIndicesSQL(StringBuilder query) {
        for (DbColumn column : mColumns) {
            if (column.mIndexed) {
                query.append("CREATE INDEX IF NOT EXISTS ");
                query.append(getIndexName(mDbTable, column.mColumnName));
                query.append(" ON ");
                query.append(mDbTable);
                query.append("(");
                query.append(column.mColumnName);
                query.append("); ");
            }
        }
    }

    public String[] getTableNameArray() {
        List<String> tableList = new ArrayList<String>();
        tableList.add(mDbTable);
        for (DbColumn column : mColumns) {
            if (column.mIndexed) {
                tableList.add(getIndexName(mDbTable, column.mColumnName));
            }
        }
        String[] result = new String[tableList.size()];
        return tableList.toArray(result);
    }

    public String getMainTableName() {
        return mDbTable;
    }

    private static String getIndexName(String dbTable, String dbColumn) {
        return INDEX_PREFIX + INDEX_SEPARATOR + dbTable + INDEX_SEPARATOR + dbColumn;
    }

    public String getInsertSQL() {

        StringBuilder valuesSb = new StringBuilder(") VALUES (");
        StringBuilder querySb = new StringBuilder("INSERT OR REPLACE INTO ");
        querySb.append(mDbTable);
        querySb.append("(");
        boolean firstColumn = true;
        for (DbColumn column : mColumns) {
            if (firstColumn) {
                firstColumn = false;
            } else {
                querySb.append(", ");
                valuesSb.append(", ");
            }
            querySb.append(column.mColumnName);
            valuesSb.append("?");
        }
        valuesSb.append(");");
        querySb.append(valuesSb);

        return querySb.toString();
    }

}
