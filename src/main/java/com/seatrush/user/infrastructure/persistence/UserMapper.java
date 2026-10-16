package com.seatrush.user.infrastructure.persistence;

import com.seatrush.user.application.query.UserView;
import com.seatrush.user.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ObjectFactory;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public abstract class UserMapper {

    public abstract UserJpaEntity toJpaEntity(User user);

    public abstract User fromEntity(UserJpaEntity jpaEntity);

    public abstract UserView toView(UserJpaEntity jpaEntity);

    @ObjectFactory
    protected User restoreUser(UserJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        return User.restore(
                jpaEntity.getId(),
                jpaEntity.getUsername(),
                jpaEntity.getEmail(),
                jpaEntity.getPassword(),
                jpaEntity.getFirstName(),
                jpaEntity.getLastName(),
                jpaEntity.getRole(),
                jpaEntity.getStatus(),
                jpaEntity.getCreatedAt()
        );
    }
}
