import { Box } from '@hh.ru/magritte-ui';
import './Sidebar.css';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { addQuestion } from '@/entities/Survey/Survey.slice';
export function Sidebar() {
    const dispatch = useAppDispatch();
    return (
        <Box height='fit-content' className='sidebar'>
            <button onClick={() => dispatch(addQuestion())}>+</button>
            <button>=</button>
        </Box>
    );
}
