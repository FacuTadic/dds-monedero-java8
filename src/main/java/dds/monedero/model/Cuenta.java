package dds.monedero.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

public class Cuenta {

  private double saldo;
  private List<Movimiento> movimientos = new ArrayList<>();

  public Cuenta(double montoInicial) {
    saldo = montoInicial;
  }

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }
  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }
  public double getSaldo() {
    return saldo;
  }




  public void agregarDinero(double cantidadDinero) {
    chequeoCantidadDineroPositiva(cantidadDinero);
    chequeoDepositosDiariosConsumidos(movimientos);

    agregateA(new Movimiento(LocalDate.now(), cantidadDinero, true));
  }


  public void retirarDinero(double cantidadDinero) {

    chequeoCantidadDineroPositiva(cantidadDinero);
    chequeoNoRetirarMasSaldoDelQueHay(cantidadDinero);

    //Innecesaria utilizacion de variable, no es algo que nos interese guardar y no aporta mucha "facilidad"
    double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
    double limite = 1000 - montoExtraidoHoy;

    chequeoDeExtraccionDiaria(cantidadDinero,limite);
    agregateA(new Movimiento(LocalDate.now(), cantidadDinero, false));
  }

  public void agregarMovimiento(Movimiento movimiento) {
    movimientos.add(movimiento);
  }

  //Medio encadenado tod0
  public double getMontoExtraidoA(LocalDate fecha) {
    return getMovimientos().stream()
        .filter(movimiento -> !movimiento.isDeposito() && movimiento.getFecha().equals(fecha))
        .mapToDouble(Movimiento::getMonto)
        .sum();
  }


  //Rompe encapsulamiento, no le corresponde esa responsabilidad
  public void agregateA(Movimiento movimiento) {
    setSaldo(calcularValor(movimiento));
    agregarMovimiento(movimiento);
  }

  //Rompe encapsulamiento, no le corresponde esa responsabilidad
  public double calcularValor(Movimiento movimiento) {
    if (movimiento.isDeposito()) {
      return saldo + movimiento.getMonto();
    } else {
      return saldo - movimiento.getMonto();
    }
  }



  public void chequeoCantidadDineroPositiva(double cantidadDinero){
    if (cantidadDinero <= 0) {
      throw new MontoNegativoException(cantidadDinero + ": el monto a ingresar debe ser un valor positivo");
    }
  }

  public void chequeoDepositosDiariosConsumidos(List<Movimiento> movimientos){
    if (movimientos.stream().filter(movimiento -> movimiento.isDeposito()).count() >= 3) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }
  }

  public void chequeoNoRetirarMasSaldoDelQueHay(double cantidadDinero){
    if (saldo - cantidadDinero < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
  }

  public void chequeoDeExtraccionDiaria(double cantidadDinero, double limite){
    if (cantidadDinero > limite) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + 1000
              + " diarios, límite: " + limite);
    }
  }

}
