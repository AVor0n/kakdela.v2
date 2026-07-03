import type { TRootState } from '@/entities/store';
import { useSelector, type TypedUseSelectorHook } from 'react-redux';

export const useAppSelector: TypedUseSelectorHook<TRootState> = useSelector;
