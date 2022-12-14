package com.example.jpa_postgresql.framework.component;

/**
 * <pre>
 * com.example.jpa_postgresql
 * └ ColumnDefinition
 *
 *
 * </pre>
 *
 * @author : hycho
 * @date : 2022-12-01
 **/
public class ColumnDefinition {

    private String columnName;

    private String definition;

    private String columnDefinition;

    private int position = Integer.MAX_VALUE - 10;

    public ColumnDefinition(String columnDefinition) {
        try {
            columnDefinition = columnDefinition.replaceAll("^\\s+", "");

            this.columnDefinition = columnDefinition;

            if (columnDefinition.toLowerCase().startsWith("primary key")) {
                position = Integer.MAX_VALUE;
            } else {
                this.columnName = columnDefinition.split(" ")[0];
                this.definition = columnDefinition.split(" ")[1];
            }
        } catch (Exception e) {
            // ignore
        }
    }

    public String getColumnDefinition() {
        return columnDefinition;
    }

    public String getColumnName() {
        return columnName;
    }

    public int getPosition() {
        return position;
    }

    public String getDefinition() {
        return definition;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
