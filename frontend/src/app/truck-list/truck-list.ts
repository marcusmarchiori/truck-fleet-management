import { Component, OnInit } from '@angular/core';
import { TruckService, Truck } from '../services/truck';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-truck-list',
  templateUrl: './truck-list.html',
  styleUrls: ['./truck-list.css'],
  imports: [
    CommonModule,
    RouterLink
  ]
})

export class TruckListComponent implements OnInit {
  trucks: Truck[] = [];
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(private truckService: TruckService, private router: Router) { }

  ngOnInit(): void {
    this.listTrucks();
  }

  listTrucks(): void {
    this.isLoading = true;
    this.truckService.getTrucks().subscribe({
      next: (data: Truck[]) => {
        this.trucks = data;
        this.errorMessage = '';
        this.isLoading = false;
      },
      error: (err: any) => {
        this.errorMessage = 'Erro ao carregar a lista de caminhões. Tente novamente mais tarde.';
        console.error('There was an error!', err);
        this.isLoading = false;
      }
    });
  }

  editTruck(id: number): void {
    this.router.navigate(['/trucks/edit', id]);
  }
}
