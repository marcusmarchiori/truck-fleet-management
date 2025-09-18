import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Truck {
  id?: number;
  licensePlate: string;
  brand: string;
  model: string;
  manufacturingYear: number;
  fipePrice?: number;
}

export interface FipeOption {
  codigo: string;
  nome: string;
}

export interface FipePriceResponse {
  Valor: string;
  Marca: string;
  Modelo: string;
  AnoModelo: number;
  Combustivel: string;
  CodigoFipe: string;
  MesReferencia: string;
  SiglaCombustivel: string;
  TipoVeiculo: number;
}

@Injectable({ providedIn: 'root' })
export class TruckService {

  private apiUrl = 'http://localhost:8080/api/trucks';
  private httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

  constructor(private http: HttpClient) {}

  getTrucks(): Observable<Truck[]> {
    return this.http.get<Truck[]>(this.apiUrl);
  }

  getTruckById(id: number): Observable<Truck> {
    return this.http.get<Truck>(`${this.apiUrl}/${id}`);
  }

  getBrands(): Observable<FipeOption[]> {
    return this.http.get<FipeOption[]>(`${this.apiUrl}/fipe/brands`);
  }

  getModels(brandCode: string): Observable<FipeOption[]> {
    return this.http.get<FipeOption[]>(`${this.apiUrl}/fipe/brands/${brandCode}/models`);
  }

  getYears(brandCode: string, modelCode: string): Observable<FipeOption[]> {
    return this.http.get<FipeOption[]>(
      `${this.apiUrl}/fipe/brands/${brandCode}/models/${modelCode}/years`
    );
  }

  getPrice(brandCode: string, modelCode: string, yearCode: string): Observable<FipePriceResponse> {
    return this.http.get<FipePriceResponse>(
      `${this.apiUrl}/fipe/brands/${brandCode}/models/${modelCode}/years/${yearCode}`
    );
  }

  createTruck(payload: Truck): Observable<Truck> {
    return this.http.post<Truck>(this.apiUrl, payload, this.httpOptions);
  }

  updateTruck(id: number, payload: Truck): Observable<Truck> {
    return this.http.put<Truck>(`${this.apiUrl}/${id}`, payload, this.httpOptions);
  }
  
}
