package co.studycards.materia;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;

/**
 * RQ-16: historial de estudio. Cada vez que el estudiante marca una tarjeta
 * durante una sesión, se guarda un registro con el resultado y el tiempo
 * que tardó. Es un registro histórico: guarda los ids como números (sin
 * llave foránea) y se limpia cuando se elimina la tarjeta o la materia.
 */
@Entity
public class RegistroEstudio extends PanacheEntity {

    @Column(nullable = false)
    public Long materiaId;

    @Column(nullable = false)
    public Long tarjetaId;

    /** APRENDIDA (acierto) o REPASAR. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public NivelDominio resultado;

    /** Segundos que tardó en responder. Puede ser null si no se midió. */
    public Integer tiempoSegundos;

    @Column(nullable = false)
    public LocalDateTime fecha;

    public RegistroEstudio() {
    }
}