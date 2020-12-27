package org.alter.eco.api.configuration;

import org.alter.eco.api.model.Point;
import org.jooq.Binding;
import org.jooq.BindingGetResultSetContext;
import org.jooq.BindingGetSQLInputContext;
import org.jooq.BindingGetStatementContext;
import org.jooq.BindingRegisterContext;
import org.jooq.BindingSQLContext;
import org.jooq.BindingSetSQLOutputContext;
import org.jooq.BindingSetStatementContext;
import org.jooq.Converter;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.Objects;

public class CoordinateBinding implements Binding<Object, Point> {

    @Override
    public Converter<Object, Point> converter() {
        return new Converter<>() {
            @Override
            public Point from(Object t) {
                return Point.fromString("" + t);
            }

            @Override
            public Object to(Point u) {
                return u.toString();
            }

            @Override
            public Class<Object> fromType() {
                return Object.class;
            }

            @Override
            public Class<Point> toType() {
                return Point.class;
            }
        };
    }

    @Override
    public void sql(BindingSQLContext<Point> ctx) {
        if (ctx.render().paramType() == ParamType.INLINED) {
            ctx.render().visit(DSL.inline(ctx.convert(converter()).value())).sql("::point");
        } else {
            ctx.render().sql("?::point");
        }
    }

    // Registering VARCHAR types for JDBC CallableStatement OUT parameters
    @Override
    public void register(BindingRegisterContext<Point> ctx) throws SQLException {
        ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
    }

    // Converting the Point to a String value and setting that on a JDBC PreparedStatement
    @Override
    public void set(BindingSetStatementContext<Point> ctx) throws SQLException {
        ctx.statement()
            .setString(ctx.index(), Objects.toString(ctx.convert(converter()).value(), null));
    }

    // Getting a String value from a JDBC ResultSet and converting that to a Point
    @Override
    public void get(BindingGetResultSetContext<Point> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.resultSet().getString(ctx.index()));
    }

    // Getting a String value from a JDBC CallableStatement and converting that to a Point
    @Override
    public void get(BindingGetStatementContext<Point> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.statement().getString(ctx.index()));
    }

    @Override
    public void set(BindingSetSQLOutputContext<Point> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void get(BindingGetSQLInputContext<Point> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}
