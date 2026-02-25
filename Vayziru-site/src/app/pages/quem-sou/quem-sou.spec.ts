import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuemSou } from './quem-sou';

describe('QuemSou', () => {
  let component: QuemSou;
  let fixture: ComponentFixture<QuemSou>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuemSou]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QuemSou);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
