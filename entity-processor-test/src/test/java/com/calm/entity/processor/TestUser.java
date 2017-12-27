package com.calm.entity.processor;

import com.calm.entity.UserBuilder;
import org.junit.Test;

public class TestUser {
    public TestUser() {

    }
    @Test
    public void  test(){
        UserBuilder builder = UserBuilder.createBuilder();
        Object[] objects = builder.buildArgs();
        String s = builder.buildQuery();
        System.out.println(int.class);
    }
}
