import { Injectable } from '@angular/core';
import { ChoiceDto, QuestionDto, QuizDto, QuizzesApi } from '@c4-soft/quiz-api';

export interface ChoiceImportExportDto {
  label: string;
  isGood?: boolean;
}

export interface QuestionImportExportDto {
  priority: number;
  label?: string;
  formattedBody?: string;
  choices?: Array<ChoiceImportExportDto>;
  comment: string;
}

export interface QuizImportExportDto {
  title: string;
  questions: Array<QuestionImportExportDto>;
}

@Injectable({
  providedIn: 'root',
})
export class QuizImportExportService {
  constructor(private api: QuizzesApi) {}

  export(quiz: QuizDto) {
    const dto = this.mapQuiz(quiz);
    const blob = this.toJsonBlob(dto);
    const linkElement = document.createElement('a');
    linkElement.href = URL.createObjectURL(blob);
    linkElement.download = `${quiz?.title || 'quiz'}.json`;
    linkElement.click();
    window.URL.revokeObjectURL(linkElement.href);
  }

  import(quiz: QuizImportExportDto) {
    this.api
      .createQuiz(
        {
          title: quiz.title,
          isChoicesShuffled: true,
          isPerQuestionResult: false,
          isReplayEnabled: false,
          isTrainerNotifiedOfNewTests: false,
        },
        'response'
      )
      .subscribe((quizCreationResp) => {
        const quizId = +(quizCreationResp.headers.get('Location') || '');
        const questionIds: number[] = quiz.questions.map((q) => 0);
        for (let i = 0; i < quiz.questions.length; i++) {
          const q = quiz.questions[i];
          this.api
            .addQuestion(
              quizId,
              {
                label: q.label || '',
                formattedBody: q.formattedBody || '',
                comment: q.comment,
              },
              'response'
            )
            .subscribe((questionCreationResp) => {
              const questionId = +(
                questionCreationResp.headers.get('Location') || ''
              );
              questionIds[i] = questionId;
              q.choices?.forEach((c) => {
                this.api
                  .addChoice(quizId, questionId, {
                    label: c.label,
                    isGood: c.isGood,
                  })
                  .subscribe();
              });
            });
        }
        this.api.updateQuestionsOrder(quizId, questionIds).subscribe();
      });
  }

  private toJsonBlob(data: unknown): Blob {
    return new Blob([JSON.stringify(data)], {
      type: 'application/json',
    });
  }

  private mapChoice(choice: ChoiceDto): ChoiceImportExportDto {
    return {
      label: choice.label,
      isGood: choice.isGood,
    };
  }

  private mapQuestion(question: QuestionDto): QuestionImportExportDto {
    return {
      priority: question.priority,
      label: question.label,
      formattedBody: question.formattedBody,
      choices: question.choices?.map(this.mapChoice),
      comment: question.comment,
    };
  }

  private mapQuiz(quiz: QuizDto): QuizImportExportDto {
    return {
      title: quiz.title,
      questions: quiz.questions.map(this.mapQuestion),
    };
  }
}
