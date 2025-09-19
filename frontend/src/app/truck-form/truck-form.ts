import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TruckService, Truck, FipeOption, FipePriceResponse } from '../services/truck';

const PLATE_REGEX = /^(?:[A-Z]{3}\d{4}|[A-Z]{3}\d[A-Z]\d{2})$/;

@Component({
  selector: 'app-truck-form',
  templateUrl: './truck-form.html',
  styleUrls: ['./truck-form.css'],
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, ReactiveFormsModule]
})
export class TruckFormComponent implements OnInit {
  truckForm!: FormGroup;
  isEditMode = false;
  truckId!: number;
  errorMessage = '';
  isLoading = false;

  brands: FipeOption[] = [];
  models: FipeOption[] = [];
  years:  FipeOption[] = [];
  fipePrice: number | null = null;

  constructor(
    private fb: FormBuilder,
    private truckService: TruckService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.truckForm = this.fb.group({
      licensePlate: ['', [Validators.required, Validators.minLength(7), Validators.maxLength(7), Validators.pattern(PLATE_REGEX)]],
      brand: ['', Validators.required],
      model: ['', Validators.required],
      yearCode: ['', Validators.required],
      manufacturingYear: [{ value: '', disabled: true }]
    });

    this.truckForm.get('licensePlate')!.valueChanges.subscribe(v => {
      if (typeof v !== 'string') return;
      const norm = v.replace(/[^A-Za-z0-9]/g, '').toUpperCase().slice(0, 7);
      if (norm !== v) {
        this.truckForm.get('licensePlate')!.setValue(norm, { emitEvent: false });
      }
    });

    this.truckService.getBrands().subscribe({
      next: (data: FipeOption[]) => ( this.brands = data || []),
      error: (err) => console.error('Erro ao carregar marcas', err)
    });

    const brandCtrl = this.truckForm.get('brand')!;
    brandCtrl.valueChanges.subscribe((brandCode: string) => {
      this.models = [];
      this.years = [];
      this.fipePrice = null;
      this.truckForm.patchValue(
        { model: '', yearCode: '', manufacturingYear: '' },
        { emitEvent: false }
      );

      if (!brandCode) return;

      this.truckService.getModels(encodeURIComponent(brandCode)).subscribe({
        next: (m: FipeOption[]) => (this.models = m || []),
        error: (e) => console.error('Erro ao carregar modelos', e)
      });
    });

    const modelCtrl = this.truckForm.get('model')!;
    modelCtrl.valueChanges.subscribe((modelCode: string) => {
      this.years = [];
      this.fipePrice = null;
      this.truckForm.patchValue(
        { yearCode: '', manufacturingYear: '' },
        { emitEvent: false }
      );

      const brandCode = this.truckForm.value.brand as string;
      if (!brandCode || !modelCode) return;

      this.truckService.getYears(
        encodeURIComponent(brandCode),
        encodeURIComponent(modelCode)
      ).subscribe({
        next: (y: FipeOption[]) => (this.years = y || []),
        error: (e) => console.error('Erro ao carregar anos', e)
      });
    });

    const yearCtrl = this.truckForm.get('yearCode')!;
    yearCtrl.valueChanges.subscribe((yearCode: string) => {
      this.fipePrice = null;

      const brandCode = this.truckForm.value.brand as string;
      const modelCode = this.truckForm.value.model as string;
      if (!brandCode || !modelCode || !yearCode) return;

      const yearName = this.years.find(y => y.codigo === yearCode)?.nome ?? '';
      const yearNumber = this.extractYearNumber(yearName);
      this.truckForm.patchValue({ manufacturingYear: yearNumber }, { emitEvent: false });

      this.truckService.getPrice(
        encodeURIComponent(brandCode),
        encodeURIComponent(modelCode),
        encodeURIComponent(yearCode)
      ).subscribe({
        next: (p: FipePriceResponse) => (this.fipePrice = this.parseCurrency(p.Valor)),
        error: (e) => console.error('Erro ao buscar preço FIPE', e)
      });
    });

    this.truckId = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isNaN(this.truckId) && this.truckId > 0) {
      this.isEditMode = true;
      this.loadTruck();
    }
  }

  private loadTruck(): void {
    this.isLoading = true;
    this.truckService.getTruckById(this.truckId).subscribe({
      next: (truck: Truck) => {
        this.truckForm.patchValue({
          licensePlate: truck.licensePlate,
          manufacturingYear: truck.manufacturingYear
        });
        this.fipePrice = truck.fipePrice ?? null;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Erro ao carregar caminhão para edição.';
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.truckForm.invalid) {
      this.errorMessage = 'Preencha todos os campos corretamente.';
      return;
    }

    const brandName = this.brands.find(b => b.codigo === this.truckForm.value.brand)?.nome ?? '';
    const modelName = this.models.find(m => m.codigo === this.truckForm.value.model)?.nome ?? '';

    const payload: Truck = {
      licensePlate: this.truckForm.value.licensePlate,
      brand: brandName,
      model: modelName,
      manufacturingYear: this.truckForm.getRawValue().manufacturingYear,
      fipePrice: this.fipePrice ?? undefined
    };

    this.isLoading = true;

    if (this.isEditMode) {
      this.truckService.updateTruck(this.truckId, payload).subscribe({
        next: () => this.router.navigate(['/trucks']),
        error: (err) => {
          this.errorMessage = err?.message || 'Erro ao atualizar caminhão.';
          this.isLoading = false;
        },
        complete: () => (this.isLoading = false)
      });
    } else {
      this.truckService.createTruck(payload).subscribe({
        next: () => this.router.navigate(['/trucks']),
        error: (err) => {
          this.errorMessage = err?.message || 'Erro ao cadastrar caminhão.';
          this.isLoading = false;
        },
        complete: () => (this.isLoading = false)
      });
    }
  }

  private extractYearNumber(name: string): number | '' {
    const y = parseInt((name || '').split('-')[0], 10);
    if (!isNaN(y) && y !== 32000) return y;
    return '';
  }

  private parseCurrency(ptBr: string): number {
    return Number((ptBr || '').replace(/[R$\s.]/g, '').replace(',', '.'));
  }
}
