package co.studycards.materia;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;

/**
 * Entidad mínima de Tarjeta. Por ahora solo la necesitamos para que HU004
 * pueda saber si una materia tiene tarjetas asociadas antes de eliminarla.
 * Los endpoints para crear/editar tarjetas se harán en sus propias historias.
 */
@Entity
public class Tarjeta extends PanacheEntity {

    @NotBlank(message = "La pregunta es obligatoria")
    @Column(nullable = false, length = 500)
    public String pregunta;

    @NotBlank(message = "La respuesta es obligatoria")
    @Column(nullable = false, length = 1000)
    public String respuesta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "materia_id")
    public Materia materia;

    public Tarjeta() {
    }
}