import classNames from 'classnames';
import { Link } from 'react-router-dom';
import style from './ProductLogo.module.css';

type Props = {
    to: string;
    className?: string;
};

export function ProductLogo({ to, className }: Props) {
    return (
        <Link className={classNames(style.logo, className)} to={to}>
            kakdela v2
        </Link>
    );
}
