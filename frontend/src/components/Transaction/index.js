import React, {Component} from 'react';

import BASE_URL from '../../Constants';

import { Link } from 'react-router-dom';

import { withRouter } from 'react-router-dom';

import './Tx.css';

class Transaction extends Component {
    constructor(props) {
        super(props);
    }

    render() {

        const { txid, sender, receiver, value, inputs, outputs, verified , isSent} = this.props;

        const outputsList = outputs.map((output,index) => (
            <li key={index}>
                Receiver: <Link to={"/address/"+ encodeURIComponent(output.to) }>{output.to} </Link> {output.value} TC
            </li>
        ));

        return (
            <div className="transaction">
                <div>
                Txid: <Link to={"/tx/"+ txid} > {txid} </Link>
                </div>
                    
                <div>
                Value: {(isSent != true) ? <span> - {value} </span> : <span> {value} </span>} 
                </div>

                {/* add inputs */}

                <div>
                    Sender: { (sender === "") ? "COINBASE" : <Link to={"/address/"+ encodeURIComponent(sender) }> {sender} </Link>} 
                    <ul>
                        { outputsList }
                    </ul>
                </div>

                <div>
                    { verified == true ?  <h3 id="confirmed-tx">CONFIRMED</h3> : <h3 id="unconfirmed-tx">UNCONFIRMED</h3>}
                </div>
            </div>
        );
    }
}

export default withRouter(Transaction);