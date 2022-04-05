package uk.gov.hmcts.cft.idam.api.v2.common.jpa;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class PostgreSqlEnumType extends org.hibernate.type.EnumType {

    public void nullSafeSet(
        PreparedStatement st,
        Object value,
        int index,
        SharedSessionContractImplementor session)
        throws HibernateException, SQLException {
        st.setObject(
            index,
            value != null ? ((Enum) value).name() : null,
            Types.OTHER
        );
    }
}
