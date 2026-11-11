package cl.zthorken.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class DireccionEnvio {

    @Column(name = "envio_calle")
    private String calle;
    @Column(name = "envio_departamento")
    private String departamento;
    @Column(name = "envio_region")
    private String region;
    @Column(name = "envio_comuna")
    private String comuna;
    @Column(name = "envio_indicaciones", length = 500)
    private String indicaciones;

    public DireccionEnvio() {
    }

    public DireccionEnvio(String calle, String departamento, String region, String comuna, String indicaciones) {
        this.calle = calle;
        this.departamento = departamento;
        this.region = region;
        this.comuna = comuna;
        this.indicaciones = indicaciones;
    }

    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }
    public String getIndicaciones() { return indicaciones; }
    public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }
}
