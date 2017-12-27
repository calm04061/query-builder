//package com.calm.entity;
//
//
//import com.calm.entity.processor.EqQueryConditional;
//
//public class UserBuilder extends com.calm.entity.processor.AbstractQueryBuilder implements com.calm.entity.processor.QueryBuilder {
//    private UserBuilder() {
//        super(com.calm.entity.processor.User.class);
//    }
//
//    public static UserBuilder createBuilder() {
//        return new UserBuilder();
//    }
//
//    public UserBuilder andIdEq(java.lang.String id) {
//        if (id != null) {
//            and(new EqQueryConditional("id", id));
//        }
//        return this;
//    }
//
//    public UserBuilder andNameEq(java.lang.String name) {
//        if (name != null) {
//            and(new EqQueryConditional("name", name));
//        }
//        return this;
//    }
//    public UserBuilder orNameEq(java.lang.String name) {
//        if (name != null) {
//            or(new EqQueryConditional("name", name));
//        }
//        return this;
//    }
//    public UserBuilder end(){
//        endPre();
//        return this;
//    }
//
//    public static void main(String[] args) {
//        String s = UserBuilder.createBuilder().andIdEq("1").orNameEq("adfad").andIdEq("1").end().orNameEq("adfs").buildQuery();
//        System.out.println(s);
//    }
//}
