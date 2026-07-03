import { useDispatch } from 'react-redux';
import type { TAppDispatch } from '@/entities/store';

export const useAppDispatch: () => TAppDispatch = useDispatch;
