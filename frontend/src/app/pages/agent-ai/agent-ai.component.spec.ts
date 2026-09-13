import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AgentAiComponent } from './agent-ai.component';

describe('AgentAiComponent', () => {
  let component: AgentAiComponent;
  let fixture: ComponentFixture<AgentAiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AgentAiComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AgentAiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
