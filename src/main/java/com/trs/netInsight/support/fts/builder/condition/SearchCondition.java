package com.trs.netInsight.support.fts.builder.condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.trs.netInsight.util.StringUtil;

/**
 * Hybase检索条件
 *
 * Create by yan.changjiang on 2017年11月20日
 */
public class SearchCondition implements Comparable<SearchCondition> {

    public enum Conjunction {
        Or, And
    }

    private List<SearchCondition> childConditions;

    private List<String> additionalTRSLs;

    /**
     * 字段名
     */
    private String field;

    /**
     * 字段值列表
     */
    private List<String> values;

    /**
     * 检索运算符
     */
    private Operator operator;

    /**
     * 子条件连接符
     */
    private Conjunction childConjunction = Conjunction.Or;

    public SearchCondition(String field, String value, Operator operator) {
        this.field = field;
        this.values = Collections.singletonList(value);
        this.operator = operator;
    }

    public SearchCondition(String field, String[] values,
                           Operator operator) {
        this.field = field;
        this.values = Arrays.asList(values);
        if (this.values.contains(null)) {
            throw new IllegalArgumentException(
                    "Condition'values contains null!");
        }
        Collections.sort(this.values);
        this.operator = operator;
    }

    public SearchCondition() {
    }

    public SearchCondition addChildCondition(
            SearchCondition condition) {
        if (childConditions == null) {
            childConditions = new ArrayList<SearchCondition>();
        }
        childConditions.add(condition);
        return this;
    }

    public SearchCondition addChildCondition(String trsl) {
        if (additionalTRSLs == null) {
            additionalTRSLs = new ArrayList<String>();
        }
        if (!StringUtil.isEmpty(trsl)) {
            additionalTRSLs.add("(" + trsl + ")");
        }
        return this;
    }

    public SearchCondition setChildConjunction(Conjunction conjunction) {
        this.childConjunction = conjunction;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof SearchCondition)) {
            return false;
        }

        SearchCondition other = (SearchCondition) obj;

        return new EqualsBuilder().append(this.field, other.field)
                .append(this.operator, other.operator)
                .append(this.values, other.values)
                .append(this.childConditions, other.childConditions)
                .append(this.childConjunction, other.childConjunction)
                .append(this.additionalTRSLs, other.additionalTRSLs).isEquals();

    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.field).append(this.operator)
                .append(this.values).append(this.childConditions)
                .append(this.childConjunction).append(this.additionalTRSLs)
                .toHashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (!StringUtil.isEmpty(field) && values != null
                && values.size() > 0) {
            switch (operator) {
                case Equal:
                    sb.append("(").append(field).append(":(")
                            .append(String.join(" OR ", values))
                            .append("))");
                    break;
                case NotEqual:
                    sb.append("NOT ").append("(").append(field).append(":(")
                            .append(String.join(" OR ", values))
                            .append("))");
                    break;
                case Between:
                    sb.append("(").append(field).append(":[").append(values.get(0))
                            .append(" TO ").append(values.get(1)).append("])");
                    break;
                case Like:
                    sb.append("(").append(field).append(":(")
                            .append(String.join("* OR ", values))
                            .append("*))");
                    break;
                case NotLike:
                    sb.append("NOT ").append("(").append(field).append(":(")
                            .append(String.join("* OR ", values))
                            .append("*))");
                    break;
                case GreaterThan:
                    sb.append("(").append(field).append(":{").append(values.get(0))
                            .append(" TO ").append("*})");
                    break;
                case GreaterThanOrEqual:
                    sb.append("(").append(field).append(":[").append(values.get(0))
                            .append(" TO ").append("*})");
                    break;
                case LessThan:
                    sb.append("(").append(field).append(":{*").append(" TO ")
                            .append(values.get(0)).append("})");
                    break;
                case LessThanOrEqual:
                    sb.append("(").append(field).append(":{*").append(" TO ")
                            .append(values.get(0)).append("])");
                    break;
                default:
                    // Unknown operator
                    throw new IllegalStateException("Unknown operator.");
            }
        }
        if (childConditions != null && !childConditions.isEmpty()) {
            Collections.sort(childConditions);
            if (sb.length() > 0) {
                sb.append(" AND ");
            }
            sb.append("(")
                    .append(StringUtil.join(childConditions.toArray(),
                            childConjunction == Conjunction.And ? " AND "
                                    : " OR "))
                    .append((additionalTRSLs == null || additionalTRSLs
                            .isEmpty()) ? ""
                            : childConjunction == Conjunction.And ? " AND "
                            : " OR ")
                    .append((additionalTRSLs == null || additionalTRSLs
                            .isEmpty()) ? "" : String.join(childConjunction == Conjunction.And
                            ? " AND " : " OR ", additionalTRSLs))
                    .append(")");
        }
        return sb.toString();
    }

    /**
     * @see Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(SearchCondition o) {
        if (o == null) {
            return 1;
        } else {
            String c1 = this.toString();
            String c2 = o.toString();
            // 时间排前
            if (c1.contains("FIELD_URLDATE")
                    || c1.contains("FIELD_CREATED_DATE")) {
                c1 = "!" + c1;
            }
            if (c2.contains("FIELD_URLDATE")
                    || c2.contains("FIELD_CREATED_DATE")) {
                c2 = "!" + c2;
            }
            // NOT检索排后
            if (Operator.NotEqual.equals(this.operator)
                    || Operator.NotLike.equals(this.operator)) {
                c1 = "~" + c1;
            }
            if (Operator.NotEqual.equals(o.operator)
                    || Operator.NotLike.equals(o.operator)) {
                c2 = "~" + c2;
            }
            return c1.compareTo(c2);
        }

    }

    /**
     * @return the field
     */
    public String getField() {
        return field;
    }

}
