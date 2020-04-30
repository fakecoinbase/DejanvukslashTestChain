import React, { Component } from 'react';
import { Route, Switch } from 'react-router-dom';

import Header from '../Header';

import './App.css';

class App extends Component {
    constructor(props) {
      super(props);
    }
  
    render() {
      return (
        <div className="App">
          <Header />
          <div id="main">
            <Switch>
              <Route path="/" exact={true} component={Home} />
              <Route path='/block/:hash' component={Block} />
              <Route path='/tx/:txid' component={Transaction} />
            </Switch>
          </div>
        </div>
      );
    }
  }
  

export default App;