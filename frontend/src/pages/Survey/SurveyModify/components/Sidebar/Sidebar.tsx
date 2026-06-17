import { Box } from '@hh.ru/magritte-ui';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { addQuestion } from '@/entities/Survey/Survey.slice';

import style from './Sidebar.module.css';
export function Sidebar() {
    const dispatch = useAppDispatch();
    return (
        <Box height='fit-content' className={style.container}>
            <button onClick={() => dispatch(addQuestion())}>+</button>
        </Box>
    );
}
