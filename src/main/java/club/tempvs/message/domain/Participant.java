package club.tempvs.message.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
@Entity
@EqualsAndHashCode(of = {"id"})
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Participant {

    @Id
    private Long id;

    private String name;

    @NotBlank
    private String type;

    @NotNull
    private String period;

    @CreatedDate
    private Instant createdDate;

    public Participant(Long id, String name, String type, String period) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.period = period;
    }
}
