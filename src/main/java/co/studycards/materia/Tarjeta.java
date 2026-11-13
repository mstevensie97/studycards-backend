package co.studycards.materia;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * Entidad Tarjeta. Los endpoints para crear/editar tarjetas se harán en sus
 * propias historias; por ahora la usamos para contar tarjetas por materia.
 */
@Entity
public class Tarjeta extends PanacheEntity {

    /** Días que tiene una tarjeta nueva para ser aprendida antes de vencer. */
    public static final int DIAS_PARA_REPASO = 5;

    @NotBlank(message = "La pregunta es obligatoria")
    @Column(nullable = false, length = 500)
    public String pregunta;

    @NotBlank(message = "La respuesta es obligatoria")
    @Column(nullable = false, length = 1000)
    public String respuesta;

    /**
     * Toda tarjeta nueva arranca como REPASAR (pendiente de repaso).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public NivelDominio nivelDominio = NivelDominio.REPASAR;

    /**
     * HU013: fecha a partir de la cual la tarjeta se considera vencida.
     * Al crearla se fija en hoy + DIAS_PARA_REPASO. Las tarjetas creadas antes
     * de esta historia no tienen fecha (null) y cuentan como vencidas.
     */
    public LocalDate fechaProximoRepaso;

    @ManyToOne(optional = false)
    @JoinColumn(name = "materia_id")
    public Materia materia;

    public Tarjeta() {
    }
}