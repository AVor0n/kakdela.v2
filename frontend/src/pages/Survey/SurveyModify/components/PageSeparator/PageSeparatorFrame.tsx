import type { ReactNode } from 'react';
import style from './PageSeparator.module.css';

type Props = {
    children: ReactNode;
    leadingAction?: ReactNode;
    trailingAction?: ReactNode;
    overlay?: ReactNode;
};

export function PageSeparatorFrame({ children, leadingAction, trailingAction, overlay }: Props) {
    const hasSideActions = Boolean(leadingAction || trailingAction);

    return (
        <div className={`${style.separator} ${hasSideActions ? '' : style.separatorWithoutActions}`}>
            {overlay}
            {leadingAction}
            <span className={style.line} aria-hidden='true' />
            <div className={style.content}>{children}</div>
            <span className={style.line} aria-hidden='true' />
            {trailingAction}
        </div>
    );
}
