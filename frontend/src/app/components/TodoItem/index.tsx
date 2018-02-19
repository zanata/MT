import * as React from 'react';
import classNames from 'classnames';
import * as style from './style.css';
import { TodoTextInput } from '../TodoTextInput';
import { applyMiddleware } from 'redux';
import {Action} from 'redux-actions';

export interface Props {
  todo: TodoItemData;
  editTodo: (todo: TodoItemData) => Action<TodoItemData>;
  deleteTodo: (id: number) => Action<number>;
  completeTodo: (id: number) => Action<number>;
}

export interface State {
  editing: boolean;
}

export class TodoItem extends React.Component<Props, State> {

  // tslint:disable-next-line:no-any
  constructor(props?: Props, context?: any) {
    super(props, context);
    this.state = {
      editing: false
    };
    this.handleSave = this.handleSave.bind(this);
    this.handleDoubleClick = this.handleDoubleClick.bind(this);
  }

  public render() {
    const { todo, completeTodo, deleteTodo } = this.props;

    let element;
    if (this.state.editing) {
      element = (
        <TodoTextInput text={todo.text}
          editing={this.state.editing}
          onSave={(text) => this.handleSave(todo.id, text)} />
      );
    } else {
      element = (
        <div className={style.view}>
          <input className={style.toggle}
            type="checkbox"
            checked={todo.completed}
            onChange={() => completeTodo(todo.id)} />

          <label onDoubleClick={this.handleDoubleClick}>
            {todo.text}
          </label>

          <button className={style.destroy} onClick={() => deleteTodo(todo.id)} />
        </div>
      );
    }

    // TODO: compose
    const classes = classNames({
      [style.completed]: todo.completed,
      [style.editing]: this.state.editing,
      [style.normal]: !this.state.editing
    });

    return (
      <li className={classes}>
        {element}
      </li>
    );
  }

  private handleDoubleClick() {
    this.setState({ editing: true });
  }

  private handleSave(id: number, text: string): void {
    if (text.length === 0) {
      this.props.deleteTodo(id);
    } else {
      this.props.editTodo({ id, text });
    }
    this.setState({ editing: false });
  }

}
