package com.calm.entity.processor;

import com.calm.entity.processor.conditional.QueryConditional;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbstractQueryBuilder implements QueryBuilder {
    private List<String> queries = new ArrayList<>();
    private QueryConditional lastConditional;
    private List<QueryConditional> conditionals = new ArrayList<>();
    private OptionType optionType = OptionType.AND;

    public AbstractQueryBuilder(Class<?> clazz) {

    }

    @Override
    public String buildQuery() {
        return StringUtils.join(queries, " ") + " )";
    }

    @Override
    public Object[] buildArgs() {
        List<Object> objects = new ArrayList<>();
        for (QueryConditional conditional : conditionals) {
            Object[] args = conditional.getArgs();
            if (args == null) {
                continue;
            }
            objects.addAll(Arrays.asList(args));
        }
        return objects.toArray();
    }

    protected void and(QueryConditional conditional) {
        if (optionType == null) {
            queries.add("AND");
        }
        if (lastConditional == null) {
            lastConditional = conditional;
            queries.add("(");
        } else {
            queries.add("AND");
        }

        optionType = OptionType.AND;
        queries.add(conditional.buildQuery());
        conditionals.add(conditional);
    }

    protected void or(QueryConditional conditional) {
        if (optionType == null) {
            queries.add("OR");
        }
        if (lastConditional == null) {
            lastConditional = conditional;
            queries.add("(");
        } else {
            queries.add("OR");
        }

        optionType = OptionType.OR;
        queries.add(conditional.buildQuery());
        conditionals.add(conditional);
    }

    public void endPre() {
        lastConditional = null;
        optionType = null;
        queries.add(")");
    }
}
