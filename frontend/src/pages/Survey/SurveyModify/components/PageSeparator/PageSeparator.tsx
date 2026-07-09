import { deleteSurveyPage } from '@/api/surveyPages';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { deletePage } from '@/entities/Survey/Survey.slice';
import type { Page } from '@/shared/types/Survey.type';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import style from './PageSeparator.module.css';
interface Props {
    page: Page;
}

export function PageSeparator({ page }: Props) {
    const dispatch = useAppDispatch();
    const deletePageHandler = () => {
        deleteSurveyPage(page.id)
            .then(() => {
                dispatch(deletePage({ pageId: page.id }));
            })
            .catch((err) => {
                if (err.response) {
                    dispatch(setErrorMessage({ message: 'Не удалось удалить страницу' }));
                }
            });
    };

    return (
        <>
            <div className={style.separator}>
                <span className={style.content}>Страница {page.serialNumber}</span>
                <img className={style.trash} src='/trash.svg' alt='X' onClick={deletePageHandler} />
            </div>
        </>
    );
}
