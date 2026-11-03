import { TestBed } from '@angular/core/testing';
import { LogoComponent } from './logo.component';

describe('LogoComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [LogoComponent] }).compileComponents();
  });

  it('apresenta a marca Help Desk com a finalidade do produto', () => {
    const fixture = TestBed.createComponent(LogoComponent);
    fixture.detectChanges();

    const logo = fixture.nativeElement as HTMLElement;
    expect(logo.textContent).toContain('Help Desk');
    expect(logo.textContent).toContain('Suporte técnico interno');
    expect(logo.textContent).not.toContain('FixLab');
  });

  it('mantém a marca legível no tema escuro', () => {
    const fixture = TestBed.createComponent(LogoComponent);
    fixture.componentRef.setInput('tema', 'escuro');
    fixture.detectChanges();

    const logo = fixture.nativeElement as HTMLElement;
    expect(logo.querySelector('.logo')?.classList.contains('logo-escuro')).toBe(true);
    expect(logo.textContent).toContain('Help Desk');
  });
});
