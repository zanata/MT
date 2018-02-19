import * as React from 'react';
import * as TodoActions from '../../actions/todos';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { RouteComponentProps } from 'react-router';
import { RootState } from '../../reducers';
import { Header, MainSection } from '../../components';

export interface Props extends RouteComponentProps<void> {
  todos: TodoItemData[];
  actions: typeof TodoActions;
}

@connect(mapStateToProps, mapDispatchToProps)
export class App extends React.Component<Props, {}> {

  public render() {
    const { todos, actions, children } = this.props;
    return (
      <div>
        <Header addTodo={actions.addTodo} />
        <MainSection todos={todos} actions={actions} />
        {children}
      </div>
    );
  }
}

function mapStateToProps(state: RootState) {
  return {
    todos: state.todos
  };
}

function mapDispatchToProps(dispatch) {
  return {
    // tslint:disable-next-line:no-any
    actions: bindActionCreators(TodoActions as any, dispatch)
  };
}
