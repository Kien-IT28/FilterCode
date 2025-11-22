package com.devk.filtercode.model;

public class PartNo {
    private int id;
    private String partNo;
    private String groupName;
    private String testMethod;
    private String classify;
    private String factory;

    // Constructor khi INSERT (không có ID)
    public PartNo(String partNo, String groupName, String testMethod, String classify, String factory) {
        this.partNo = partNo;
        this.groupName = groupName;
        this.testMethod = testMethod;
        this.classify = classify;
        this.factory = factory;
    }

    // Constructor khi SELECT (có ID)
    public PartNo(int id, String partNo, String groupName, String testMethod, String classify, String factory) {
        this.id = id;
        this.partNo = partNo;
        this.groupName = groupName;
        this.testMethod = testMethod;
        this.classify = classify;
        this.factory = factory;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getTestMethod() {
        return testMethod;
    }

    public void setTestMethod(String testMethod) {
        this.testMethod = testMethod;
    }

    public String getClassify() {
        return classify;
    }

    public void setClassify(String classify) {
        this.classify = classify;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }
}
/*
Tại sao cần 2 constructor?

Constructor không id → dùng để INSERT
Vì ID được database tự tang (AUTO_INCREMENT).

Constructor có id → dùng khi SELECT từ DB
Vì lúc này đã có id để set vào object.
 */